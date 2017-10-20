package cn.skyeye.aptrules;

import cn.skyeye.aptrules.ioc2rules.Ioc2RuleHandler;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
import cn.skyeye.redis.RedisContext;
import com.google.common.collect.Maps;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

/**
 * Description:
 *   aptrules项目的上下文
 *   缺少必要的日志记录
 * @author LiXiaoCong
 * @version 2017/10/11 17:34
 */
public class ARContext {

    private static final String ID = "AR";

    private volatile static ARContext arContext;

    private ARConf arConf;
    private RedisContext redisContext;
    private Ioc2RuleHandler ioc2RuleHandler;

    private ARContext(){
        this.arConf = new ARConf();
        initRedis();
    }

    public static ARContext get(){
        if(arContext == null){
            synchronized (ARContext.class){
                if(arContext == null){
                    arContext = new ARContext();
                }
            }
        }
        return arContext;
    }

    private void initRedis() {
        Map<String, String> redisConf = Maps.newHashMap();
        redisConf.put("redis.host", arConf.getConfigItemValue("redis.host", "localhost"));
        redisConf.put("redis.port", arConf.getConfigItemValue("redis.port", "6379"));

        this.redisContext = RedisContext.get();
        redisContext.initRedisConnPool(ID, redisConf);
    }

    public Jedis getJedis(){
        return this.redisContext.getRedisConn(ID);
    }

    public ARConf getArConf() {
        return arConf;
    }

    public synchronized Ioc2RuleHandler getIoc2RuleHandler() {
        if(ioc2RuleHandler == null){
            ioc2RuleHandler = new Ioc2RuleHandler();
        }
        return ioc2RuleHandler;
    }

    public static void main(String[] args) {
        ARContext arContext = ARContext.get();
        Ioc2RuleHandler ioc2RuleHandler = arContext.getIoc2RuleHandler();

        arContext.getJedis();

        ioc2RuleHandler.syncRule();

        Map<String, Object> record = Maps.newHashMap();
        //dport":35975,"dip":"23.234.19.114
        record.put("dport", 35975);
        record.put("dip", "23.234.19.114");
        record.put("name", "jechedo");

        List<VagueRule> vagueRules = ioc2RuleHandler.getRuler().matchRules(record);
        System.out.println(vagueRules);


    }
}
