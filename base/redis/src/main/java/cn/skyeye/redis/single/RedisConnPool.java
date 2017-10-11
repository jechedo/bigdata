package cn.skyeye.redis.single;

import cn.skyeye.redis.RedisBaseConf;
import cn.skyeye.resources.ConfigDetail;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/11 16:20
 */
public class RedisConnPool {
    private final Logger logger = Logger.getLogger(RedisConnPool.class);

    private String id;
    private JedisPool jedisPool;
    private ConfigDetail conf;

    public RedisConnPool(String id, RedisBaseConf baseConf, Map<String, String> extraConf){
        this.id = id;
        Map<String, String> confConfigs = baseConf.getConfigs();
        if(extraConf == null){
            this.conf = new ConfigDetail(confConfigs);
        }else{
            confConfigs.putAll(extraConf);
            this.conf = new ConfigDetail(confConfigs);
        }
        logger.info(String.format("RedisConnPool：%s的配置为：\n  %s", id, conf.getConfigMap()));

        JedisPoolConfig jedisPoolConfig = newPoolConfig();

        String host = conf.getConfigItemValue("redis.host");
        int port= conf.getConfigItemInteger("redis.port", 6379);

        this.jedisPool= new JedisPool(jedisPoolConfig, host, port);
    }

    private JedisPoolConfig newPoolConfig(){

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        int maxIdle = conf.getConfigItemInteger("redis.MaxIdle", 20);
        jedisPoolConfig.setMaxIdle(maxIdle);//最大空闲链接

        int maxTotal = conf.getConfigItemInteger("redis.MaxTotal", 20);
        jedisPoolConfig.setMaxTotal(maxTotal);//最大全部链接

        int maxWaitMillis = conf.getConfigItemInteger("redis.MaxWaitMillis", 60000);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);//最大超时时间

        jedisPoolConfig.setTestOnBorrow(true);//测链接是否可用

        return jedisPoolConfig;
    }

    public Jedis getRedisConn(){
        return this.jedisPool.getResource();
    }

    public boolean hasIdleRedisConn(){
        return getPoolNumIdle() > 0;
    }

    public void destroyPool(){
        this.jedisPool.destroy();
    }

    public void closePool(){
        if(!this.jedisPool.isClosed()) {
            this.jedisPool.close();
        }
    }

    public int getActiveRedisConnNum(){
        return this.jedisPool.getNumActive();
    }

    //空闲
    public int getPoolNumIdle(){
        return this.jedisPool.getNumIdle();
    }

    public int getNumWaiters(){
        return this.jedisPool.getNumWaiters();
    }

    public String getId() {
        return id;
    }
}
