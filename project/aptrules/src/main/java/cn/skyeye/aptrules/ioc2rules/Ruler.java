package cn.skyeye.aptrules.ioc2rules;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ARContext;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
import cn.skyeye.common.databases.DataBases;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
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
    void overrideRules(List<VagueRule> vagueRules){
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

        Set<String> columns = vagueRules.get(0).getRecord().keySet();

        DataBases dataBases = DataBases.get(arConf.getConn());
        DataBases.InsertBatch insertBatch = dataBases.insertBatch(table, columns);

        int n = 0;
        Set<String> roleIndexKeys;
        for(VagueRule vagueRule : vagueRules){
            insertBatch.add(vagueRule.getRecord());

            //添加到缓存
            rulesCache.add(vagueRule);
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

    public List<VagueRule> matchRules(Map<String, Object> record){
        String indexKey = getIndexKey(record);
        return matchRules(record, indexKey);
    }

    /**
     * 合成indexkey 可能会合成多个
     * @param record
     * @return
     */
    private String getIndexKey(Map<String, Object> record){

        List<String> roleIndexFieldLevels = arConf.getRoleIndexFieldLevels();
        StringBuilder indexKey = new StringBuilder();
        Object o;
        for(String field : roleIndexFieldLevels){
            o = record.get(field); //取值处可能需要根据数据进行调整
            if(o != null){

            }
        }

        return null;
    }


    /**
     *  由日志数据中的告警字段合成 ruleKey， 然后再规则中查找是否存在对应的告警规则。
     *  从缓存中查询。
     * @param record
     * @param indexKey
     * @return   null  or  Collection<VagueRule>
     */
    private List<VagueRule> matchRules(Map<String, Object> record, String indexKey){
        this.lock.lock();
        try {
            Set<Integer> cacheIds = rulesIndexs.get(indexKey);
            if(cacheIds.size() > 0){
                List<VagueRule> res = Lists.newArrayList();
                VagueRule rule;
                for(Integer cacheId : cacheIds){
                    rule = rulesCache.get(cacheId);
                    if(rule.matches(record, indexKey)){
                        res.add(rule);
                    }
                }
                return res;
            }
        } finally {
            this.lock.unlock();
        }

        return Lists.newArrayList();
    }

    public static void main(String[] args) {

    }
}
