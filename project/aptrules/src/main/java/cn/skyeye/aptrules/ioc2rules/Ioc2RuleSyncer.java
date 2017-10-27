package cn.skyeye.aptrules.ioc2rules;

import cn.skyeye.aptrules.ioc2rules.iocs.IoCer;
import cn.skyeye.aptrules.ioc2rules.rules.Ruler;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Description:
 *  负责将Ioc转换成告警规则
 *  由于是实时处理：
 *     即变成数据找规则的模式，考虑使用 一个ioc对应一个rule
 *  离线处理的情况下，是规则找数据，可以是多个ioc对应一个rule
 * @author LiXiaoCong
 * @version 2017/10/11 18:17
 */
public class Ioc2RuleSyncer {

    private final Logger logger = Logger.getLogger(Ioc2RuleSyncer.class);
    private final Lock lock = new ReentrantLock();

    private IoCer ioCer;
    private Ruler ruler;

    public Ioc2RuleSyncer(){
        this.ioCer = new IoCer();
        this.ruler = new Ruler();
    }

    public void syncCloud(){
        try{
            this.lock.lock();
            logger.info("开始同步云端中的ioc到rule。");

            List<VagueRule> rules = ioCer.listIoCAsRuleInNet();
            if(!rules.isEmpty()){
                ruler.syncNetRule2MemoryAndDB(rules);
                logger.info("同步云端中的ioc到rule成功。");
            }
            ioCer.updateNetStoreStatus(true);
        }catch (Exception e){
            ioCer.updateNetStoreStatus(false);
            logger.error("同步云端中的ioc到rules失败。", e);
        }finally {
            this.lock.unlock();
        }

    }

    /**
     * 数据库规则同步
     */
    public void syncDataBase(){
        try{
            this.lock.lock();
            logger.info("开始同步数据库中的ioc到rule。");

            List<VagueRule> rules = ioCer.listIoCAsRuleInDB();
            if(!rules.isEmpty()){
                ruler.syncDBRule2MemoryAndDB(rules);
                logger.info("同步数据库中的ioc到rule成功。");
            }
            ioCer.updateDBStoreStatus(true);
        }catch (Exception e){
            ioCer.updateDBStoreStatus(false);
            logger.error("同步数据库中的ioc到rules失败。", e);
        }finally {
            this.lock.unlock();
        }
    }

    public Ruler getRuler() {
        return ruler;
    }
}
