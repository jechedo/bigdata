package cn.skyeye.aptrules.ioc2rules;

import cn.skyeye.aptrules.ARContext;
import cn.skyeye.common.SysEnvs;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

/**
 * Description:
 *  负责将Ioc转换成告警规则
 *  由于是实时处理：
 *     即变成数据找规则的模式，考虑使用 一个ioc对应一个rule
 *  离线处理的情况下，是规则找数据，可以是多个ioc对应一个rule
 * @author LiXiaoCong
 * @version 2017/10/11 18:17
 */
public class Ioc2RuleHandler {

    private final Logger logger = Logger.getLogger(Ioc2RuleHandler.class);

    private final String last_time_key = "ioc2rule-last-timestamp";
    private final String last_ioccount_key = "ioc-count-last-time";

    private long stime = 0L;
    private long etime;

    private ARContext arContext;
    private IoCer ioCs;

    public Ioc2RuleHandler(ARContext arContext) throws Exception{
        this.arContext = arContext;
        String defaultDBPath = String.format("%s/skyeye.db", SysEnvs.getJarFileDirByClass(Ioc2RuleHandler.class));
        String dbPath = arContext.getArConf().getConfigItemValue("ar.db.path", defaultDBPath);
        this.ioCs = new IoCer(dbPath);
    }


    public void execute(){
        Jedis jedis = null;
        try{
            jedis = arContext.getJedis();
            initActiveTimeRange(jedis);

            if(isModified(jedis)){
                executeIoc2Rule();
            }else {
                logger.warn("no new ioc, just exit!");
            }
        }catch (Exception e){
            logger.error("执行ioc到rules转换失败。", e);
            e.printStackTrace();
        }finally {
            if(jedis != null)jedis.close();
        }
    }

    private void executeIoc2Rule(){

    }

    private void initActiveTimeRange(Jedis jedis) {
        String lastActiveTime = jedis.get(last_time_key);
        if (lastActiveTime != null) {
            stime = Long.parseLong(lastActiveTime);
        }
        etime = System.currentTimeMillis();
    }

    /**
     * 判断sqlite中的ioc表是否有变动，判断规则为:
     *    判断sqlite中ioc总量是否与redis中记录的最后一次处理的ioc总量相等
     *    若不想等，则返回true,
     *    若相等，则判断从redis中记录的最后一次处理ioc时间到当前时间的范围内，是否有ioc记录变更，
     *    若有 则返回true, 否则返回false
     *
     * 查询sqlite失败的时候返回false.
     */
    private boolean isModified(Jedis jedis) {
        int redisIocCount = 0;
        String iocCountStr = jedis.get(last_ioccount_key);
        if(iocCountStr != null){
            redisIocCount = Integer.parseInt(iocCountStr);
        }

        //查询sqlite 获取当前ioc的总量 进行比较
        int dbIocCount = -1;
        try {
            dbIocCount = ioCs.iocCount();
        } catch (Exception e) {
           logger.error("获取ioc的总数失败。", e);
        }

        if(dbIocCount > -1){
            jedis.set(last_ioccount_key, String.valueOf(dbIocCount));
            try {
                return (dbIocCount != redisIocCount || ioCs.iocActiveBetweenCount(stime, etime) > 0);
            } catch (Exception e) {
                logger.error(String.format("获取时间范围[%s, %s]内的有效ioc失败。", stime, etime), e);
            }
        }

        return false;
    }

}
