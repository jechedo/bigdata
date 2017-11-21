package cn.skyeye.norths.syslog;

import cn.skyeye.common.json.Jsons;
import cn.skyeye.norths.NorthsConf;
import cn.skyeye.norths.utils.AlarmLogFilter;
import cn.skyeye.resources.ConfigDetail;
import org.apache.commons.lang.StringUtils;
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

    private NorthsConf northsConf;
    private AlarmLogFilter alarmLogFilter;

    public SyslogConf(NorthsConf northsConf){
        this.northsConf = northsConf;
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

    public void getLogFilter(){
        String filterJson = northsConf.getSystemConfig("norths_syslog_alarm_conf");
        if(StringUtils.isNotBlank(filterJson)){

        }
    }
}
