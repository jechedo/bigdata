package cn.skyeye.norths.actions;

import cn.skyeye.norths.NorthContext;
import cn.skyeye.norths.events.DataEventHandler;
import cn.skyeye.norths.services.syslog.SyslogConf;
import cn.skyeye.norths.services.syslog.Sysloger;
import cn.skyeye.norths.utils.ResponseHelper;
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
        return sysloger.getSyslogConf().getSyslogConfig().getConfig();
    }

    @ResponseBody
    @RequestMapping(value = "syslogalarm/list", method = { RequestMethod.GET, RequestMethod.POST})
    Object getSyslogAlarmConfig(){
        Sysloger sysloger = getSysloger();
        return sysloger.getSyslogConf().getSyslogAlarmConfig().getConfig();
    }

    @ResponseBody
    @RequestMapping(value = "syslog/edit", method = { RequestMethod.POST})
    Object editSyslogConfig(@RequestBody Map<String, Object> syslogConf){
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
        Sysloger sysloger = getSysloger();
        sysloger.getSyslogConf().setSyslogAlarmConfig(syslogAlarmConf);
        sysloger.initAlarmFilter(SyslogConf.newSyslogAlarmConfig(syslogAlarmConf));
        return ResponseHelper.success();
    }

    private Sysloger getSysloger(){
        DataEventHandler syslog = northContext.getHandler("syslog");
        return (Sysloger) syslog;
    }

}
