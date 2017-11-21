package cn.skyeye.norths.syslog;

import cn.skyeye.norths.NorthsConf;
import cn.skyeye.resources.ConfigDetail;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Set;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/21 17:25
 */
public class SyslogConf extends ConfigDetail {
    private static final String CONF_PREFFIX =
            String.format("norths.handler.%s.", Sysloger.NAME);

    private final Logger logger = Logger.getLogger(SyslogConf.class);

    private Set<String> acceeptSources;
    private Set<String> excludes;
    private Set<String> includes;

    public SyslogConf(NorthsConf northsConf){
        Map<String, String> config = northsConf.getConfigMapWithPrefix(CONF_PREFFIX);
        this.configMap.putAll(config);
        this.acceeptSources = getConfigItemSet(String.format("%sdatasources", CONF_PREFFIX));
        this.excludes = getConfigItemSet(String.format("%excludes", CONF_PREFFIX));
        this.includes = getConfigItemSet(String.format("%includes", CONF_PREFFIX));
    }
    

    public boolean isAcceeptSource(String source){
        return this.acceeptSources.isEmpty() ? true : this.acceeptSources.contains(source);
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public Set<String> getIncludes() {
        return includes;
    }
}
