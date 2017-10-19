package cn.skyeye.aptrules.ioc2rules;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ARContext;
import cn.skyeye.aptrules.ioc2rules.rules.Rule;
import cn.skyeye.common.databases.DataBases;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
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

    private String table = "rules";

    private ARConf arConf;

    public Ruler(){
        this.arConf = ARContext.get().getArConf();
        //同步sqlite中的rule
        listRules();
    }

    public void listRules(){

        this.lock.lock();
        try {

        }catch (Exception e){
            logger.error("读取sqlite中的规则失败。", e);
        }finally {
            this.lock.unlock();
        }

    }

    /**
     *  覆盖sqlite中原有的rule信息
     *  逻辑为  先删除再插入
     */
    public void overrideRules(){
        this.lock.lock();
        try {
            deleteRules();
            insertRules();
        }catch (Exception e){
            logger.error("覆盖sqlite中的规则失败。", e);
        }finally {
            this.lock.unlock();
        }
    }

    private void deleteRules() throws Exception {
        String sql = String.format("delete * from %s where rule_id >= ? and rule_id <= ?", table);

        Connection conn = arConf.getConn();
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setLong(1, arConf.getCustomRuleIdStart());
        preparedStatement.setLong(2, arConf.getCustomRuleIdEnd());

        preparedStatement.executeUpdate();

        DataBases.close(preparedStatement);
    }

    private void insertRules(){

    }

    /**
     *  由日志数据中的告警字段合成 ruleKey， 然后再规则中查找是否存在对应的告警规则。
     *  从缓存中查询。
     * @param ruleKey
     * @return   null  or  Collection<Rule>
     */
    public Collection<Rule> findRules(String ruleKey){
        return null;
    }

    public static void main(String[] args) {

    }
}
