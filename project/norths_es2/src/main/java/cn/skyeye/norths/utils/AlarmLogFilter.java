package cn.skyeye.norths.utils;

import cn.skyeye.norths.services.syslog.SyslogConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
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
        List<String> levels = syslogAlarmConfig.getLevel();
        if(!levels.isEmpty()){
            Object hazardLevelObj = alarmLog.get("hazard_level");
            if(hazardLevelObj == null)return false;

            try {
                int hazardLevel = Integer.parseInt(String.valueOf(hazardLevelObj));


            } catch (NumberFormatException e) {
                logger.error(String.format("下面告警日志的威胁级别hazard_level不是Int类型：\n\t %s", alarmLog), e);
                return false;
            }


        }
        return true;
    }

    private boolean confidenceAccept(Map<String, Object> alarmLog){
        List<String> confidences = syslogAlarmConfig.getConfidence();
        if(!confidences.isEmpty()){
            //获取确信度
            Object confidenceObj = alarmLog.get("confidence");
            if(confidenceObj == null)return true;
            try {
                int confidenceScore = Integer.parseInt(String.valueOf(confidenceObj));
                String confidence;
                if(confidenceScore <= 50){
                    confidence = "低";
                }else if(confidenceScore > 50 && confidenceScore <= 80){
                    confidence = "中";
                }else {
                    confidence = "高";
                }
                return confidences.contains(confidence);
            } catch (NumberFormatException e) {
                logger.error(String.format("下面告警日志的确信度confidence不是Int类型：\n\t %s", alarmLog), e);
                return false;
            }
        }
        return true;
    }

    private boolean statusAccept(Map<String, Object> alarmLog){
        List<String> statuss = syslogAlarmConfig.getStatus();
        if(!statuss.isEmpty()){
            //获取资产
            Object assetObj = alarmLog.get("_asset");
            if(assetObj == null) return false;

            try {
                Map<String, Object> asset = (Map<String, Object>) assetObj;
                Object statusObj = asset.get("host_state");
                return statuss.contains(statusObj);
            } catch (Exception e) {
                logger.error(String.format("下面告警日志的资产信息host_state格式有误。\n\t %s", alarmLog));
                return false;
            }
        }
        return true;
    }

    private boolean logtypeAccept(Map<String, Object> alarmLog){
        List<String> logtypes = syslogAlarmConfig.getLogtype();
        if(!logtypes.isEmpty()){
            Object typeObj = alarmLog.get("type");
            if(typeObj == null) return false;
        }
        return true;
    }

}
