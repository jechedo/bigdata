package cn.skyeye.norths.utils;

import cn.skyeye.norths.services.syslog.SyslogConf;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Description:
 *
 *  根据客户配置的告警筛选规则进行告警日志筛选：
 *  Json格式：
     {
     level: “all”,    //威胁级别
     confidence: “all”,  //确信度
     status: “all”,  //主机状态
     logtype:[“APT”,”攻击利用”…]   //日志种类
     }
 *
 * @author LiXiaoCong
 * @version 2017/11/21 19:34
 */
public class AlarmLogFilter {

    private final Log logger = LogFactory.getLog(AlarmLogFilter.class);
    private SyslogConf.SyslogAlarmConfig syslogAlarmConfig;

    public AlarmLogFilter(SyslogConf.SyslogAlarmConfig syslogAlarmConfig){
        this.syslogAlarmConfig = syslogAlarmConfig;
    }

    public boolean isAccept(Map<String, Object> alarmLog){
        boolean b = levelAccept(alarmLog);
        if(b)b = confidenceAccept(alarmLog);
        if(b)b = statusAccept(alarmLog);
        if(b)b = logtypeAccept(alarmLog);
        return b;
    }

    private boolean levelAccept(Map<String, Object> alarmLog){
        String level = syslogAlarmConfig.getLevel();
        if(StringUtils.isNotBlank(level) && !"all".equalsIgnoreCase(level)){

        }
        return true;
    }

    private boolean confidenceAccept(Map<String, Object> alarmLog){
        String level = syslogAlarmConfig.getLevel();
        if(StringUtils.isNotBlank(level) && !"all".equalsIgnoreCase(level)){

        }
        return true;
    }

    private boolean statusAccept(Map<String, Object> alarmLog){
        String level = syslogAlarmConfig.getLevel();
        if(StringUtils.isNotBlank(level) && !"all".equalsIgnoreCase(level)){

        }
        return true;
    }

    private boolean logtypeAccept(Map<String, Object> alarmLog){
        String level = syslogAlarmConfig.getLevel();
        if(StringUtils.isNotBlank(level) && !"all".equalsIgnoreCase(level)){

        }
        return true;
    }

}
