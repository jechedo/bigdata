package cn.skyeye.elasticsearch;

import cn.skyeye.resources.ConfigDetail;
import cn.skyeye.resources.Resources;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/25 10:26
 */
public class EsBaseConf extends ConfigDetail {

    private static final String _CONFIG = "/elasticsearch/elasticsearch-base";
    private static final Map<String, String> ENVCONF = Maps.newHashMap();

    private final Logger logger = Logger.getLogger(EsBaseConf.class);

    private boolean hasDefault;
    private List<String> clientIds;
    private String defaultClientId;

    private boolean loadOnStart;

    static {
        System.getProperties().forEach((keyObj, valueObj) -> {
            String key = keyObj.toString();
            String value = valueObj.toString();
            if(key.toLowerCase().startsWith("es.")){
                ENVCONF.put(key, value);
            }
        });
    }

    EsBaseConf(boolean loadEnv){
        super();
        if(loadEnv) this.configMap.putAll(ENVCONF);
        try {
            Resources resources = new Resources(Resources.Env.NONE, _CONFIG);
            this.configMap.putAll(resources.getConfigMap());
        } catch (Exception e) {
            logger.error("读取es基础配置失败。", e);
        }

        this.clientIds = getConfigItemList("es.clients");
        this.hasDefault = (!clientIds.isEmpty());
        if(hasDefault) {
            this.defaultClientId = getConfigItemValue("es.default.client", clientIds.get(0));
        }

        this.loadOnStart = getConfigItemBoolean("es.load.on.start", true);
    }

    public boolean hasDefault() {
        return hasDefault;
    }

    public List<String> getClientIds() {
        return clientIds;
    }

    public String getDefaultClientId() {
        return defaultClientId;
    }

    public boolean isLoadOnStart() {
        return loadOnStart;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EsBaseConf{");
        sb.append(configMap).append('}');
        return sb.toString();
    }
}
