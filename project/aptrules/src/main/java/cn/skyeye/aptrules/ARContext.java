package cn.skyeye.aptrules;

import cn.skyeye.aptrules.ioc2rules.Ioc2RuleHandler;
import cn.skyeye.redis.RedisContext;
import com.google.common.collect.Maps;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * Description:
 *   aptrules项目的上下文
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
}
