package cn.skyeye.aptrules.ioc2rules.rules;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ARContext;
import cn.skyeye.aptrules.ioc2rules.rules.stores.RuleMemoryStore;
import cn.skyeye.aptrules.ioc2rules.rules.stores.RuleSQLiteStore;
import cn.skyeye.aptrules.ioc2rules.rules.stores.RuleStore;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Description:
 *     rules表的相关操作
 *     目前其职责考虑为：
 *         1. 将Ioc2RuleHandler推过来的是rule存入到sqlite(严格意义上算是覆盖)
 *         2. 将Ioc2RuleHandler推过来的是rule缓存再内存（本地内存或推入redis），供实时告警查询
 *         3. 在启动时主动同步sqlite中的rule
 * @author LiXiaoCong
 * @version 2017/10/13 17:09
 */
public class Ruler {
    public enum RuleID {custom, cloud}

    private final Logger logger = Logger.getLogger(Ruler.class);
    private final Lock lock = new ReentrantLock();

    private ARConf arConf;
    private RuleMemoryStore memoryCache;
    private RuleStore dataBaseStore;

    public Ruler(){
        this.arConf = ARContext.get().getArConf();
        this.memoryCache = new RuleMemoryStore(arConf);
        this.dataBaseStore = new RuleSQLiteStore(arConf);

        //同步sqlite中的rule到缓存
        syncDBRule2MemoryCache();
    }

    private void syncDBRule2MemoryCache(){
        logger.info("系统启动，同步sqlite中的rule到内存。");

        List<VagueRule> vagueRules;
        try {
            vagueRules = dataBaseStore.allRules(RuleID.custom);
            memoryCache.overrideRules(RuleID.custom, vagueRules);
            logger.info(String.format("同步sqlite中%s的rule到内存成功， ruleCount = %s。",RuleID.custom, vagueRules.size()));
        } catch (Exception e) {
            logger.error(String.format("同步sqlite中%s的rule到内存失败。",RuleID.custom), e);
        }

        try {
            vagueRules = dataBaseStore.allRules(RuleID.cloud);
            memoryCache.overrideRules(RuleID.cloud, vagueRules);
            logger.info(String.format("同步sqlite中%s的rule到内存成功， ruleCount = %s。",RuleID.cloud, vagueRules.size()));
        } catch (Exception e) {
            logger.error(String.format("同步sqlite中%s的rule到内存失败。",RuleID.cloud), e);
        }
    }

    public void syncNetRule2MemoryAndDB(List<VagueRule> vagueRules) throws Exception {
        if(!vagueRules.isEmpty()) {
            this.lock.lock();
            try {
                dataBaseStore.overrideRules(RuleID.cloud, vagueRules);
                memoryCache.overrideRules(RuleID.cloud, vagueRules);
            } finally {
                this.lock.unlock();
            }
        }
    }

    public void syncDBRule2MemoryAndDB(List<VagueRule> vagueRules) throws Exception {
        if(!vagueRules.isEmpty()) {
            this.lock.lock();
            try {
                dataBaseStore.overrideRules(RuleID.custom, vagueRules);
                memoryCache.overrideRules(RuleID.custom, vagueRules);
            } finally {
                this.lock.unlock();
            }
        }
    }

    public Hits matchRules(Map<String, Object> record){
        Set<IndexKey> indexKeys = getIndexKey(record);
        return matchRules(indexKeys);
    }

    /**
     * 合成indexkey 可能会合成多个
     * 目前只是一个最简单的版本  没有考虑字段需要调整的情况。
     * @param record
     * @return
     */
    private Set<IndexKey> getIndexKey(Map<String, Object> record){

        List<String> roleIndexFieldLevels = arConf.getRoleIndexFieldLevels();
        Set<IndexKey> indexKeys = Sets.newHashSet(new IndexKey());
        Set<IndexKey> indexKeyModels;
        IndexKey model;
        Map<String, Object> o;
        Set<Map.Entry<String, Object>> entries;
        for(String field : roleIndexFieldLevels){
            //取值处可能需要根据数据进行调整
            // 比如 field = 'md5' , 可能需要取record中的file_md5或者process_md5 或者其他md5字段的值
            o = getIndexFieldValues(record, field);
            if(!o.isEmpty()){
                entries = o.entrySet();
                indexKeyModels = Sets.newHashSet(indexKeys);
                indexKeys = Sets.newHashSet();
                for(IndexKey ik : indexKeyModels){
                    for(Map.Entry<String, Object> entry : entries) {
                        model = new IndexKey(ik);
                        model.addKV(field, entry.getValue(),  entry.getKey(),  entry.getValue());
                        indexKeys.add(model);

                        if(arConf.isIocVagueField(field)) {
                            model = new IndexKey(ik);
                            model.addKV(field, null, entry.getKey(),  entry.getValue());
                            indexKeys.add(model);
                        }
                    }
                }

            }
        }

        return indexKeys;
    }


    private Map<String, Object> getIndexFieldValues(Map<String, Object> record, String indexField){
        Map<String, Object> res = Maps.newHashMap();
        //String field;
        Object o;
        Set<String> vals = Sets.newHashSet();
        switch (indexField){
            case "md5":
                o = record.get("md5");
                if(o != null && vals.add(String.valueOf(o))) res.put("md5", o);

                o = record.get("file_md5");
                if(o != null && vals.add(String.valueOf(o))) res.put("file_md5", o);

                o = record.get("process_md5");
                if(o != null && vals.add(String.valueOf(o))) res.put("process_md5", o);
                break;
            default:
                o = record.get(indexField);
                if(o != null && vals.add(String.valueOf(o))) res.put(indexField, o);
                break;

        }

        return res;
    }

    /**
     *  由日志数据中的告警字段合成 ruleKey， 然后再规则中查找是否存在对应的告警规则。
     *  从缓存中查询。
     * @param indexKeys
     * @return   null  or  Collection<VagueRule>
     */
    private Hits matchRules(Set<IndexKey> indexKeys){
        Hits res = new Hits();
        lock.lock();
        try {
            List<VagueRule> rules;
            for(IndexKey key : indexKeys) {
                rules = memoryCache.findRules(key);
                if(!rules.isEmpty()){
                    res.addKeyAndRules(key, rules);
                }
            }
        } finally {
            lock.unlock();
        }
        return res;
    }

    /**
     * 命中结果
     */
    public class Hits{
        private Map<IndexKey, List<VagueRule>> hitsMap;
        private Hits(){
            this.hitsMap = Maps.newHashMap();
        }

        private void addKeyAndRules(IndexKey key, List<VagueRule> rule){
            hitsMap.put(key, rule);
        }

        public Set<Map.Entry<IndexKey, List<VagueRule>>> getHitSet(){
            return hitsMap.entrySet();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Hits{");
            sb.append(hitsMap).append('}');
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        List<String> list = Lists.newArrayList();
        list.add(null);
        System.out.println(list);

    }
}
