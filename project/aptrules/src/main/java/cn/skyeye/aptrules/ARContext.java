package cn.skyeye.aptrules;

import cn.skyeye.aptrules.alarms.Alarmer;
import cn.skyeye.aptrules.alarms.stores.AlarmEsStore;
import cn.skyeye.aptrules.ioc2rules.Ioc2RuleHandler;
import cn.skyeye.aptrules.ioc2rules.Ruler;
import cn.skyeye.common.net.IPtoLong;
import cn.skyeye.redis.RedisContext;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * Description:
 *   aptrules项目的上下文
 *   缺少必要的日志记录
 * @author LiXiaoCong
 * @version 2017/10/11 17:34
 */
public class ARContext {

    private Logger logger = Logger.getLogger(ARContext.class);

    private static final String ID = "AR";

    private volatile static ARContext arContext;

    private ARConf arConf;
    private RedisContext redisContext;
    private Alarmer alarmer;
    private Ioc2RuleHandler ioc2RuleHandler;

    private long netA = ipv4ToLong("10.255.255.255") >> 24;
    private long netB = ipv4ToLong("172.31.255.255") >> 20;
    private long netC = ipv4ToLong("192.168.255.255") >> 16;

    private ARContext(){
        this.arConf = new ARConf();
        this.alarmer = new Alarmer(arConf, new AlarmEsStore());
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

    public Alarmer getAlarmer() {
        return alarmer;
    }

    public Long ipv4ToLong(String ipv4){
        long l = 0;
        try {
            l = IPtoLong.ipToLong(ipv4);
        } catch (Exception e) {
            logger.error(String.format("ipv4:%s 转整数失败。", ipv4), e);
        }
        return l;
    }

    public boolean isInternalIP(String ipv4){
        long ipData = ipv4ToLong(ipv4);
        return (ipData >> 24 == netA || ipData >> 20 == netB || ipData >> 16 == netC);
    }

    public static void main(String[] args) {
        ARContext arContext = ARContext.get();
        Ioc2RuleHandler ioc2RuleHandler = arContext.getIoc2RuleHandler();

        new Thread(new Runnable() {
            @Override
            public void run() {

                Map<String, Object> record = Maps.newHashMap();
                //dport":35975,"dip":"23.234.19.114
                record.put("dport", 35975);
                record.put("dip", "23.234.19.114");
                record.put("name", "jechedo");

                long n = 0L;
                while (true){
                    Ruler.Hits hits = ioc2RuleHandler.getRuler().matchRules(record);
                    System.out.println(n++ + " ：" + hits);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                long n = 0L;
                while (true){
                    try {
                        Thread.sleep(5000);
                        ioc2RuleHandler.syncRule();
                        System.err.println(n++);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }
}
