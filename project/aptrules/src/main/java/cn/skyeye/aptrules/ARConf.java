package cn.skyeye.aptrules;

import cn.skyeye.resources.ConfigDetail;
import cn.skyeye.resources.Resources;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Description:
 *  aptrules项目的配置
 * @author LiXiaoCong
 * @version 2017/10/11 17:35
 */
public class ARConf extends ConfigDetail{
    private static final String _CONFIG = "/aprules";

    private final Logger logger = Logger.getLogger(ARConf.class);

    private long customRuleIdStart;
    private long customRuleIdEnd;

    private Map<String, Integer> tidMap;

    ARConf(){

        System.getProperties().forEach((keyObj, valueObj) -> {
            String key = keyObj.toString();
            String value = valueObj.toString();
            if(key.toLowerCase().startsWith("ar.")){
                addConfig(key, value);
            }
        });

        try {
            Resources resources = new Resources(Resources.Env.NONE, _CONFIG);
            this.configMap.putAll(resources.getConfigMap());
        } catch (Exception e) {
            logger.error("没有读取到任何的配置。", e);
            e.printStackTrace();
            System.exit(-1);
        }

        this.customRuleIdStart = 0x2000000000000001L;
        this.customRuleIdEnd = 0x20FFFFFFFFFFFFFFL;

        this.tidMap = Maps.newHashMap();
        this.tidMap.put("自定义情报告警", 0);

    }

    public long getCustomRuleIdStart() {
        return customRuleIdStart;
    }

    public long getCustomRuleIdEnd() {
        return customRuleIdEnd;
    }
}
