package cn.skyeye.rpc.netty;

import cn.skyeye.resources.ConfigDetail;
import cn.skyeye.resources.Resources;
import cn.skyeye.rpc.netty.sasl.SecretKeyHolder;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/31 13:28
 */
public class RpcBaseConf extends ConfigDetail {
    private static final String _CONFIG = "/rpc/rpc-base";
    private static final Map<String, String> ENVCONF = Maps.newHashMap();

    private final Logger logger = Logger.getLogger(RpcBaseConf.class);

    private String systemId;
    private String hostname = "localhost";
    private SecretKeyHolder keyHolder;

    static {
        System.getProperties().forEach((keyObj, valueObj) -> {
            String key = keyObj.toString();
            String value = valueObj.toString();
            if(key.toLowerCase().startsWith("rpc.")){
                ENVCONF.put(key, value);
            }
        });
    }

    RpcBaseConf(boolean loadEnv){
        super();
        if(loadEnv) this.configMap.putAll(ENVCONF);
        try {
            Resources resources = new Resources(Resources.Env.NONE, _CONFIG);
            this.configMap.putAll(resources.getConfigMap());
        } catch (Exception e) {
            logger.error("读取rpc基础配置失败。", e);
        }

        this.systemId = getConfigItemValue("rpc.system.id", "skyeye");
        try {
            InetAddress addr = InetAddress.getLocalHost();
            this.hostname = addr.getHostName();
        } catch (UnknownHostException e) {
            logger.error(null, e);
        }

        this.keyHolder = new SecretKeyHolder() {
            @Override
            public String getSaslUser(String appId) {
                return hostname;
            }

            @Override
            public String getSecretKey(String appId) {
                return "we are skyeyeer";
            }
        };
    }

    public String getSystemId() {
        return systemId;
    }

    public String getHostname() {
        return hostname;
    }

    public SecretKeyHolder getKeyHolder() {
        return keyHolder;
    }
}
