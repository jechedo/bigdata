package cn.skyeye.norths.services.syslog;

import cn.skyeye.common.json.Jsons;
import cn.skyeye.norths.NorthsConf;
import cn.skyeye.resources.ConfigDetail;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/21 17:25
 */
public class SyslogConf {

    public static final String SYSLOG_CONF = "norths_syslog_conf";
    public static final String SYSLOG_ALARM_CONF = "norths_syslog_alarm_conf";

    private static final Log logger = LogFactory.getLog(SyslogConf.class);

    private String conf_preffix;
    private Set<String> acceptSources;
    private Map<String, Set<String>> acceptTypeMap ;
    private Set<String> excludes;
    private Set<String> includes;

    private ConfigDetail configDetail;
    private NorthsConf northsConf;

    SyslogConf(String preffix, ConfigDetail configDetail, NorthsConf northsConf){
        this.conf_preffix = preffix;
        this.configDetail = configDetail;
        this.northsConf = northsConf;
        this.acceptTypeMap = Maps.newHashMap();

        this.acceptSources = configDetail.getConfigItemSet(String.format("%sdatasources", conf_preffix));
        Set<String> acceptTypes;
        for(String acceptSource : acceptSources){
            acceptTypes = configDetail.getConfigItemSet(String.format("%sdatasources.%s.types", conf_preffix, acceptSource));
            if(!acceptTypes.isEmpty()){
                this.acceptTypeMap.put(acceptSource, acceptTypes);
            }
        }

        this.excludes = configDetail.getConfigItemSet(String.format("%sexcludes", conf_preffix));
        this.includes = configDetail.getConfigItemSet(String.format("%sincludes", conf_preffix));
    }

    public boolean isAcceptSource(String source, String type){
        boolean accept = true;
        if(!this.acceptSources.isEmpty()){
            accept = this.acceptSources.contains(source);
            if(accept){
                Set<String> types = acceptTypeMap.get(source);
                if(types != null && !types.isEmpty()){
                    accept = types.contains(type);
                }
            }
        }

        return accept;
    }

    public SyslogConfig getSyslogConfig(){
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
        return new SyslogConfig(res);
    }

    public SyslogAlarmConfig getSyslogAlarmConfig(){
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
        return new SyslogAlarmConfig(res);
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
        res.put("level", Lists.newArrayList());
        //res.put("confidence", "");
        res.put("status", Lists.newArrayList());
        res.put("logtype", Lists.newArrayList());
        return res;
    }

    public static class SyslogConfig{

        private Map<String, Object> syslogConfig;

        private SyslogConfig(Map<String, Object> syslogConfig){
            this.syslogConfig = syslogConfig;
        }

        public boolean isOpen(){
            Object obj = syslogConfig.get("switch");
            return  obj == null ? false : "1".equals(String.valueOf(obj));
        }

        public boolean threatOpen(){
            Object obj = syslogConfig.get("threat_switch");
            return  obj == null ? false : "1".equals(String.valueOf(obj));
        }

        public String getProtocol(){
            Object obj = syslogConfig.get("protocol");
            return obj == null ? "UDP" : String.valueOf(obj);
        }

        public List<Map<String, Object>> getServices(){
            Object obj = syslogConfig.get("services");
            if(obj != null){
                if(obj instanceof List){
                    return (List<Map<String, Object>>)obj;
                }else {
                    logger.error(String.format("%s的配置项services：%s配置格式有误", SYSLOG_CONF, obj));
                }
            }
            return Lists.newArrayList();
        }

        public Map<String, Object> getConfig() {
            return syslogConfig;
        }
    }

    public static class SyslogAlarmConfig{

        private Map<String, Object> syslogAlarmConfig;

        public SyslogAlarmConfig(Map<String, Object> syslogAlarmConfig){
            this.syslogAlarmConfig = syslogAlarmConfig;
        }

        public List<String> getLevel(){
            return  getListByField("level");
        }

        public List<String> getConfidence(){
            return getListByField("confidence");
        }
        public List<String> getStatus(){
            return getListByField("status");
        }

        public List<String> getLogtype(){
            return getListByField("logtype");
        }

        private List<String> getListByField(String field){
            Object obj = syslogAlarmConfig.get(field);
            if(obj != null && !"全部".equals(obj)){
                if(obj instanceof List){
                    return (List<String>)obj;
                } else if(obj instanceof String){
                    String s = String.valueOf(obj);
                    if(StringUtils.isNotEmpty(s))
                        return Lists.newArrayList(s.split(","));
                }else {
                    logger.error(String.format("%s的配置项%s：%s配置格式有误", SYSLOG_ALARM_CONF, field, obj));
                }
            }
            return Lists.newArrayList();
        }

        public Map<String, Object> getConfig() {
            return syslogAlarmConfig;
        }
    }

    public static SyslogConfig newSyslogConfig(Map<String, Object> syslogConf){
        return new SyslogConfig(syslogConf);
    }

    public static SyslogAlarmConfig newSyslogAlarmConfig(Map<String, Object> syslogAlarmConf){
        return new SyslogAlarmConfig(syslogAlarmConf);
    }

}
