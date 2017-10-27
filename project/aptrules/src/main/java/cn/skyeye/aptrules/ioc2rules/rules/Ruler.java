package cn.skyeye.aptrules.ioc2rules.rules;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ARContext;
import cn.skyeye.common.databases.DataBases;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final Logger logger = Logger.getLogger(Ruler.class);
    private final Lock lock = new ReentrantLock();
    private List<String> columns;

    private List<VagueRule> rulesCache = Lists.newArrayList();
    private HashMultimap<String, Integer> rulesIndexs = HashMultimap.create();
    private AtomicBoolean cacheNotEmpty = new AtomicBoolean(false);

    private String table = "rules";

    private ARConf arConf;

    public Ruler(){
        this.arConf = ARContext.get().getArConf();
        //同步sqlite中的rule
        listRules();
    }

    private void listRules(){

        try {
            logger.info("系统启动，同步sqlite中的rule到内存。");
            String sql = String.format("select * from %s", table);
            Statement statement = arConf.getConn().createStatement();

            ResultSet resultSet = statement.executeQuery(sql);

            if(columns == null || columns.isEmpty()){
                columns = DataBases.getColumns(resultSet);
            }

            VagueRule vagueRule;
            String jsonRuleInfo;
            int n = 0;
            Set<String> roleIndexKeys;
            while (resultSet.next()){
                vagueRule = new VagueRule(arConf);
                for(String column : columns) {
                    if("rule".equals(column)){
                        jsonRuleInfo = resultSet.getString(column);
                        vagueRule.setJsonRuleInfo(jsonRuleInfo);
                    }else{
                        vagueRule.setKV(column, resultSet.getObject(column));
                    }
                }

                rulesCache.add(vagueRule);
                roleIndexKeys = vagueRule.getRoleIndexKeys();
                for(String roleIndexKey : roleIndexKeys){
                    rulesIndexs.put(roleIndexKey, n);
                }
                n++;
            }

            cacheNotEmpty.set(n > 0);
            logger.info(String.format("同步sqlite中的rule到内存成功， ruleCount = %s。", n));
            DataBases.close(resultSet);
            DataBases.close(statement);
        }catch (Exception e){
            logger.error("读取sqlite中的规则失败。", e);
        }
    }

    /**
     *  覆盖sqlite中原有的rule信息
     *  逻辑为  先删除再插入
     */
   public void overrideRules(List<VagueRule> vagueRules){
        if(!vagueRules.isEmpty()) {
            this.lock.lock();
            try {
                deleteRules();
                insertRules(vagueRules);
            } catch (Exception e) {
                logger.error("覆盖sqlite中的规则失败。", e);
            } finally {
                this.lock.unlock();
            }
        }
    }

    private void deleteRules() throws Exception {

        //删除缓存
        this.rulesCache.clear();
        this.rulesIndexs.clear();
        cacheNotEmpty.set(false);
        logger.info("清除缓存中的rule成功。");

        //删除数据库中的数据
        String sql = String.format("delete from %s where rule_id >= ? and rule_id <= ?", table);

        Connection conn = arConf.getConn();
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setLong(1, arConf.getCustomRuleIdStart());
        preparedStatement.setLong(2, arConf.getCustomRuleIdEnd());

        preparedStatement.executeUpdate();
        logger.info("清除sqlite中的rule成功。");

        DataBases.close(preparedStatement);
    }

    private void insertRules(List<VagueRule> vagueRules) throws Exception {

        Set<String> columns = vagueRules.get(0).getRuleMap().keySet();

        DataBases dataBases = DataBases.get(arConf.getConn());
        DataBases.InsertBatch insertBatch = dataBases.insertBatch(table, columns);

        int n = 0;
        Set<String> roleIndexKeys;
        for(VagueRule vagueRule : vagueRules){
            insertBatch.add(vagueRule.getRuleMap());

            //添加到缓存
            rulesCache.add(vagueRule);
            //需要测试验证 准确性 和 稳定性
            roleIndexKeys = vagueRule.getRoleIndexKeys();
            for(String roleIndexKey : roleIndexKeys){
                rulesIndexs.put(roleIndexKey, n);
            }
            n++;
        }
        cacheNotEmpty.set(n > 0);
        insertBatch.execute();
        logger.info(String.format("rule写入缓存和sqlite成功, roleCount = %s。", n));
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
        this.lock.lock();
        Hits res = new Hits();
        VagueRule rule;
        Set<Integer> ids = Sets.newHashSet();
        try {
            Set<Integer> cacheIds;
            for(IndexKey key : indexKeys) {
                cacheIds = rulesIndexs.get(key.getRuleKey());
                cacheIds.removeAll(ids);
                for (Integer cacheId : cacheIds) {
                    rule = rulesCache.get(cacheId);
                    if (rule.matches(key)) {
                        res.addKeyAndRule(key, rule);
                    }
                }
                ids.addAll(cacheIds);
            }
        } finally {
            this.lock.unlock();
        }

        return res;
    }


    /**
     * 命中结果
     */
    public class Hits{
        private Map<IndexKey, VagueRule> hitsMap;
        private Hits(){
            this.hitsMap = Maps.newHashMap();
        }

        private void addKeyAndRule(IndexKey key, VagueRule rule){
            hitsMap.put(key, rule);
        }

        public Set<Map.Entry<IndexKey, VagueRule>> getHitSet(){
            return hitsMap.entrySet();
        }

        public int getHitSize(){
            return hitsMap.size();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Hits{");
            sb.append(hitsMap).append('}');
            return sb.toString();
        }
    }

    /**
     * 根据数据生成的查找规则的RuleKey
     *  由于Key生成的时候并不是严格按照规则中type里的字段，
     *  因此还存在一个对应的数据的DataKey
     *  DataKey和RuleKey的区别主要在于字段名称不同，内容相同，一个是对于数据而言的，一个是对于规则而言的。
     *  此对象主要是为了方便后期生成告警表单。
     */
    public class IndexKey{
        private List<String> ruleFields;
        private List<Object> ruleDatas;
        private List<String> dataFields;
        private List<Object> datas;

        private IndexKey(){
            this.ruleFields  = Lists.newArrayList();
            this.ruleDatas = Lists.newArrayList();
            this.dataFields  = Lists.newArrayList();
            this.datas     = Lists.newArrayList();
        }

        private IndexKey(IndexKey indexKey){
            this.ruleFields  = Lists.newArrayList(indexKey.ruleFields);
            this.ruleDatas = Lists.newArrayList(indexKey.ruleDatas);
            this.dataFields  = Lists.newArrayList(indexKey.dataFields);
            this.datas     = Lists.newArrayList(indexKey.datas);
        }

        private void addKV(String ruleField, Object ruleData, String dataField, Object data){
            this.ruleFields.add(ruleField);
            this.ruleDatas.add(ruleData);
            this.dataFields.add(dataField);
            this.datas.add(data);
        }

        public List<String> getRuleFields() {
            return ruleFields;
        }

        public String getDataFieldByRuleField(String ruleField){
            String dataField = null;
            int size = ruleFields.size();
            for (int i = 0; i < size; i++) {
                if(ruleField.equals(ruleFields.get(i))){
                    dataField = dataFields.get(i);
                    break;
                }
            }
            return dataField;
        }

        public Object getDataByRuleField(String ruleField){
            return getDataByFieldInDatas(ruleFields, datas, ruleField);
        }

        public String getRuleKey() {
            return getKeyString(ruleFields, ruleDatas);
        }

        public String getRuleDataKey() {
            return getKeyString(ruleFields, datas);
        }

        public List<String> getNoEmptyRuleFields(){
            List<String> res = Lists.newArrayList();
            int size = ruleDatas.size();
            for (int i = 0; i < size; i++) {
                if(ruleDatas.get(i) != null){
                    res.add(ruleFields.get(i));
                }
            }
            return res;
        }

        public List<Object> getRuleDatas(){
            return datas;
        }

        private Object getDataByFieldInDatas(List<String> fields, List<Object> datas, String field){
            Object data = null;
            int size = fields.size();
            for (int i = 0; i < size; i++) {
                if(field.equals(fields.get(i))){
                    data = datas.get(i);
                    break;
                }
            }
            return data;
        }

        private String getKeyString(List<String> fields, List<Object> datas) {
            StringBuilder sb = new StringBuilder();
            int size = fields.size();
            Object value;
            for (int i = 0; i < size; i++) {
                sb.append(",").append(fields.get(i));
                value = datas.get(i);
                if(value != null){
                    sb.append(":").append(value);
                }
            }
            return sb.substring(1);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("IndexKey{");
            sb.append("ruleFields=").append(ruleFields);
            sb.append(", ruleDatas=").append(ruleDatas);
            sb.append(", dataFields=").append(dataFields);
            sb.append(", datas=").append(datas);
            sb.append('}');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IndexKey indexKey = (IndexKey) o;
            return getRuleDataKey().equals(indexKey.getRuleDataKey());
        }

        @Override
        public int hashCode() {
            int result = ruleFields.hashCode();
            result = 31 * result + datas.hashCode();
            return result;
        }
    }

    public static void main(String[] args) {
        List<String> list = Lists.newArrayList();
        list.add(null);
        System.out.println(list);

    }
}
