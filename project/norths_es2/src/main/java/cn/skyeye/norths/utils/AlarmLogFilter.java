package cn.skyeye.norths.utils;

import cn.skyeye.common.json.Jsons;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Set;

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

    private final Logger logger = Logger.getLogger(AlarmLogFilter.class);
    private String level;
    private String confidence;
    private String status;
    private Set<String> logtyps;

    public AlarmLogFilter(String filterJson){
        Map<String, Object> filter = null;
        try {
            filter = Jsons.toMap(filterJson);
        } catch (Exception e) {
            logger.error(String.format("norths_syslog_alarm_conf的值%s不是标准json格式。", filterJson), e);
        }

        if(filter != null){
            Object levelObj = filter.get("level");//获取筛选的威胁级别
            if(levelObj != null){
                //存在筛选
                level = (String) levelObj;
            }

        }
    }


}
