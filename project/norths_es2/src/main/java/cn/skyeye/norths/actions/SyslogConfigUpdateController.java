package cn.skyeye.norths.actions;

import cn.skyeye.norths.NorthContext;
import cn.skyeye.norths.events.DataEventHandler;
import cn.skyeye.norths.services.syslog.SyslogConf;
import cn.skyeye.norths.services.syslog.Sysloger;
import cn.skyeye.norths.utils.ResponseHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/22 10:09
 */
@RestController
@RequestMapping(value="/norths/config/")
public class SyslogConfigUpdateController {
    protected final Log logger = LogFactory.getLog(SyslogConfigUpdateController.class);

    private NorthContext northContext = NorthContext.get();

    @ResponseBody
    @RequestMapping(value = "syslog/list", method = { RequestMethod.GET, RequestMethod.POST})
    Object getSyslogConfig(){
        Sysloger sysloger = getSysloger();
        Map<String, Object> config = sysloger.getSyslogConf().getSyslogConfig().getConfig();
        logger.debug(String.format("查询syslog的配置: %s", config));
        return ResponseHelper.success(config);
    }

    @ResponseBody
    @RequestMapping(value = "syslogalarm/list/default", method = { RequestMethod.GET, RequestMethod.POST})
    Object getSyslogAlarmDefault(){
        Map<String, Object> configAlarmConfig = Maps.newHashMap();
        configAlarmConfig.put("level", Lists.newArrayList("危急", "高危", "中危", "低危"));
        configAlarmConfig.put("status", Lists.newArrayList("失陷", "可疑"));
        configAlarmConfig.put("logtype", northContext.getNorthsConf().getThreats());
        return ResponseHelper.success(configAlarmConfig);
    }


    @ResponseBody
    @RequestMapping(value = "syslogalarm/list", method = { RequestMethod.GET, RequestMethod.POST})
    Object getSyslogAlarmConfig(){
        Sysloger sysloger = getSysloger();
        Map<String, Object> config = sysloger.getSyslogConf().getSyslogAlarmConfig().getConfig();
        logger.debug(String.format("查询syslog告警的配置: %s", config));
        return ResponseHelper.success(config);
    }

    @ResponseBody
    @RequestMapping(value = "syslog/edit", method = { RequestMethod.POST})
    Object editSyslogConfig(@RequestBody Map<String, Object> syslogConf){
        checkRequestParam(syslogConf);
        logger.info(String.format("编辑syslog配置: %s", syslogConf));
        Sysloger sysloger = getSysloger();
        //更新配置
        sysloger.getSyslogConf().setSyslogConfig(syslogConf);
        //更新syslog服务器
        sysloger.initSyslogClient(SyslogConf.newSyslogConfig(syslogConf));
        return ResponseHelper.success();
    }

    @ResponseBody
    @RequestMapping(value = "syslogalarm/edit", method = { RequestMethod.POST})
    Object editSyslogAlarmConfig(@RequestBody Map<String, Object> syslogAlarmConf){
        checkRequestParam(syslogAlarmConf);
        logger.info(String.format("编辑syslog告警配置: %s", syslogAlarmConf));
        Sysloger sysloger = getSysloger();
        sysloger.getSyslogConf().setSyslogAlarmConfig(syslogAlarmConf);
        sysloger.initAlarmFilter(SyslogConf.newSyslogAlarmConfig(syslogAlarmConf));
        return ResponseHelper.success();
    }

    private void checkRequestParam(Map<String, Object> param){
        if(param != null){
            param.remove("csrf_token");
            param.remove("r");
        }
    }

    private Sysloger getSysloger(){
        DataEventHandler syslog = northContext.getHandler("syslog");
        return (Sysloger) syslog;
    }

}
