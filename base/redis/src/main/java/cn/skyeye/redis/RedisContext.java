package cn.skyeye.redis;

import cn.skyeye.redis.single.RedisConnPool;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/11 15:23
 */
public class RedisContext {
    private final Logger logger = Logger.getLogger(RedisContext.class);

    private volatile static RedisContext redisContext;

    private RedisBaseConf baseConf;
    public Map<String, RedisConnPool> pools;

    private RedisContext(boolean loadEnv){
        this.baseConf = new RedisBaseConf(loadEnv);
        this.pools = Maps.newConcurrentMap();
    }

    public static RedisContext get(){
        return  get(true);
    }

    public static RedisContext get(boolean loadEnv){
        if(redisContext == null){
            synchronized (RedisContext.class){
                if(redisContext == null){
                    redisContext = new RedisContext(loadEnv);
                }
            }
        }
        return redisContext;
    }

    public synchronized RedisConnPool initRedisConnPool(String poolId, Map<String, String> conf){
        RedisConnPool redisConnPool = pools.get(poolId);
        if(redisConnPool == null){
            redisConnPool = new RedisConnPool(poolId, baseConf, conf);
            pools.put(poolId, redisConnPool);
        }
        return redisConnPool;
    }

    public RedisConnPool getRedisConnPool(String poolId){
        return pools.get(poolId);
    }

    public Jedis getRedisConn(String poolId){
        Jedis redisConn = null;
        RedisConnPool redisConnPool = getRedisConnPool(poolId);
        if(redisConnPool != null){
            redisConn = redisConnPool.getRedisConn();
        }
        return redisConn;
    }

    public synchronized void closeRedisConnPool(){
        pools.keySet().forEach(poolId -> closeRedisConnPool(poolId));
    }

    public synchronized void closeRedisConnPool(String poold){
        RedisConnPool redisConnPool = pools.get(poold);
        if(redisConnPool != null){
            try {
                redisConnPool.closePool();
                redisConnPool.destroyPool();
            }catch (Exception e){
                logger.error(String.format("关闭RedisConnPool：%s 发生异常。", poold), e);
            }
            pools.remove(poold);
        }
    }

}
