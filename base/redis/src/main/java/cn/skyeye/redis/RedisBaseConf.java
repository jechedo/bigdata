package cn.skyeye.redis;

import cn.skyeye.resources.Resources;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/11 15:26
 */
public class RedisBaseConf {
    private final Logger logger = Logger.getLogger(RedisBaseConf.class);

    private static final String _CONFIG = "/redis/redis-base";

    private Map<String, String> conf;

    RedisBaseConf(boolean loadEnv){
        this.conf = new HashMap<>();

        if(loadEnv){
            System.getProperties().forEach((keyObj, valueObj) -> {
                String key = keyObj.toString();
                String value = valueObj.toString();
                if(key.toLowerCase().startsWith("redis.")){
                    conf.put(key, value);
                }
            });
        }

        try {
            Resources resources = new Resources(Resources.Env.NONE, _CONFIG);
            conf.putAll(resources.getConfigMap());
        } catch (Exception e) {
            logger.error("读取redis基础配置失败。", e);
        }
    }

    public String getConfigValue(String key){
        return conf.get(key);
    }

    public String getConfigValue(String key, String def){
        String value = conf.get(key);
        return value == null ? def : value;
    }

    public boolean containsConfigKey(String key){
        return conf.containsKey(key);
    }

    public Map<String, String> getConfigs(){
        return new HashMap<>(conf);
    }
}
