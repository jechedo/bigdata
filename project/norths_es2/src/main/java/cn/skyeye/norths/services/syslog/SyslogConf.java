package cn.skyeye.norths.services.syslog;

import cn.skyeye.common.json.Jsons;
import cn.skyeye.norths.NorthsConf;
import cn.skyeye.norths.utils.AlarmLogFilter;
import cn.skyeye.resources.ConfigDetail;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

    public static final String SYSLOG_CONF = "norths_syslog_conf";
    public static final String SYSLOG_ALARM_CONF = "norths_syslog_alarm_conf";

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

    public Map<String, Object> getSyslogConfig(){
        String syslogConf = northsConf.getSystemConfig(SYSLOG_CONF);
        Map<String, Object> res;
        if(syslogConf == null){
           res = newDefaultSyslogConfig();
        }else {
            try {
                res = Jsons.toMap(syslogConf);
            } catch (Exception e) {
                logger.error(String.format("%s对应的值%s不是标准的json格式。", SYSLOG_CONF, syslogConf), e);
                res = newDefaultSyslogConfig();
            }
        }
        return res;
    }

    public Map<String, Object> getSyslogAlarmConfig(){
        String syslogConf = northsConf.getSystemConfig(SYSLOG_ALARM_CONF);
        Map<String, Object> res;
        if(syslogConf == null){
           res = newDefaultSyslogAlarmConfig();
        }else {
            try {
                res = Jsons.toMap(syslogConf);
            } catch (Exception e) {
                logger.error(String.format("%s对应的值%s不是标准的json格式。", SYSLOG_ALARM_CONF, syslogConf), e);
                res = newDefaultSyslogAlarmConfig();
            }
        }
        return res;
    }


    public void setSyslogConfig(Map<String, Object> syslogConf){
        String conf = Jsons.obj2JsonString(syslogConf);
        northsConf.setSystemConfig(SYSLOG_CONF, conf);
    }

    public void setSyslogAlarmConfig(Map<String, Object> syslogAlarmConf){
        String conf = Jsons.obj2JsonString(syslogAlarmConf);
        northsConf.setSystemConfig(SYSLOG_ALARM_CONF, conf);
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

    private Map<String, Object> newDefaultSyslogConfig(){
        Map<String, Object> res = Maps.newHashMap();
        res.put("switch", "0");
        res.put("protocol", "UDP");
        res.put("threat_switch", "0");
        // res.put("systemlog_switch", "0");
        res.put("services", Lists.newArrayList());
        return res;
    }

    private Map<String, Object> newDefaultSyslogAlarmConfig(){
        Map<String, Object> res = Maps.newHashMap();
        res.put("level", "");
        res.put("confidence", "");
        res.put("status", "");
        res.put("logtype", Lists.newArrayList());
        return res;
    }
}
