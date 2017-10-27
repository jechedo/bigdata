package cn.skyeye.aptrules.ioc2rules.rules.stores;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ioc2rules.rules.Ruler;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
import cn.skyeye.common.databases.DataBases;
import com.google.common.collect.Lists;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/27 9:50
 */
public class RuleSQLiteStore extends RuleStore {

    private String table = "rules";

    private List<String> columns;

    public RuleSQLiteStore(ARConf arConf) {
        super(arConf);
    }

    @Override
    public List<VagueRule> allRules(Ruler.RuleID ruleID) throws Exception {
        String sql = null;
        switch (ruleID){
            case custom:
                sql = String.format("select * from %s where rule_id >= %s and rule_id <= %s",
                        table, arConf.getCustomRuleIdStart(), arConf.getCustomRuleIdEnd());
                break;
            case cloud:
                sql = String.format("select * from %s where rule_id > %s and rule_id < %s",
                        table, arConf.getCustomRuleIdEnd(), arConf.getCustomRuleIdStart());
                break;
            default:
                logger.error(String.format("无法识别查询sqlite中rules的id", ruleID.name()));
        }
        if(sql == null){
            return Lists.newArrayList();
        }

        return queryRules(sql);
    }

    private List<VagueRule> queryRules(String sql) throws Exception {
        Statement statement = null;
        ResultSet resultSet = null;
        List<VagueRule> res = Lists.newArrayList();
        try {
            statement = arConf.getConn().createStatement();
            resultSet = statement.executeQuery(sql);
            if(columns == null || columns.isEmpty()){
                columns = DataBases.getColumns(resultSet);
            }

            VagueRule vagueRule;
            String jsonRuleInfo;
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
                res.add(vagueRule);
            }
        }finally {
            DataBases.close(resultSet);
            DataBases.close(statement);
        }
        return res;
    }

    @Override
    public void overrideRules(Ruler.RuleID ruleID, List<VagueRule> vagueRules) throws Exception {
       deleteRules(ruleID);
       insertRules(ruleID, vagueRules);
    }

    private void insertRules(Ruler.RuleID ruleID, List<VagueRule> vagueRules) throws Exception {

        Set<String> columns = vagueRules.get(0).getRuleMap().keySet();
        int size = vagueRules.size();
        VagueRule vagueRule;

        DataBases dataBases = DataBases.get(arConf.getConn());
        DataBases.InsertBatch insertBatch = dataBases.insertBatch(table, columns);

        for (int i = 0; i < size; i++) {
            vagueRule = vagueRules.get(i);
            insertBatch.add(vagueRule.getRuleMap());
        }
        insertBatch.execute();
        logger.info(String.format("%s的rule写入sqlite成功, roleCount = %s。", ruleID, size));
    }

    private void deleteRules(Ruler.RuleID ruleID) throws Exception {
        String sql = null;
        switch (ruleID){
            case custom:
                sql = String.format("delete from %s where rule_id >= %s and rule_id <= %s",
                        table, arConf.getCustomRuleIdStart(), arConf.getCustomRuleIdEnd());
                break;
            case cloud:
                sql = String.format("delete from %s where rule_id > %s and rule_id < %s",
                        table, arConf.getCustomRuleIdEnd(), arConf.getCustomRuleIdStart());
                break;
            default:
                logger.error(String.format("无法识别查询sqlite中rules的id", ruleID.name()));
        }

        if(sql != null){
            Statement statement = null;
            try {
                Connection conn = arConf.getConn();
                statement = conn.createStatement();
                statement.executeUpdate(sql);
                logger.info(String.format("清除sqlite中%s的rule成功。", ruleID));
            }  finally {
                DataBases.close(statement);
            }
        }
    }
}
