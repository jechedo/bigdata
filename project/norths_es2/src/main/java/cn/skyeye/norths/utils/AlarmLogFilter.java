package cn.skyeye.norths.utils;

import cn.skyeye.norths.services.syslog.SyslogConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

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
    private ReentrantLock lock = new ReentrantLock();

    public AlarmLogFilter(){ }

    public AlarmLogFilter(SyslogConf.SyslogAlarmConfig syslogAlarmConfig){
       initConfig(syslogAlarmConfig);
    }

    public void initConfig(SyslogConf.SyslogAlarmConfig syslogAlarmConfig){
        this.lock.lock();
        this.syslogAlarmConfig = syslogAlarmConfig;
        this.lock.unlock();
    }

    public boolean isAccept(Map<String, Object> alarmLog){
        if(syslogAlarmConfig != null) {
            this.lock.lock();
            try {
                boolean b = levelAccept(alarmLog);
                // if(b)b = confidenceAccept(alarmLog);
                if (b) b = statusAccept(alarmLog);
                if (b) b = logtypeAccept(alarmLog);
                return b;
            } finally {
                this.lock.unlock();
            }
        }
        return true;
    }

    private boolean levelAccept(Map<String, Object> alarmLog){
        List<String> levels = syslogAlarmConfig.getLevel();
        if(!levels.isEmpty()){
            Object hazardLevelObj = alarmLog.get("hazard_level");
            if(hazardLevelObj == null)return false;

            try {
                int hazardLevel = Integer.parseInt(String.valueOf(hazardLevelObj));
                String level;
                if(hazardLevel < 4){
                    level = "低危";
                }else if(hazardLevel >= 4 && hazardLevel < 6){
                    level = "中危";
                }else if(hazardLevel >= 6 && hazardLevel < 8) {
                    level = "高危";
                }else {
                    level = "危急";
                }
                return levels.contains(level);
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
                boolean contains = statuss.contains(statusObj);
                if(contains){
                    alarmLog.put("host_state", statusObj);
                }
                return contains;
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
            return logtypes.contains(typeObj);
        }
        return true;
    }

}
