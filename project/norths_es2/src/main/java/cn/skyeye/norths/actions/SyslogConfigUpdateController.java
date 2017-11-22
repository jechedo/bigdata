package cn.skyeye.norths.actions;

import cn.skyeye.norths.NorthContext;
import cn.skyeye.norths.services.syslog.Sysloger;
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
    private Sysloger sysloger = northContext.getSysloger();

    @ResponseBody
    @RequestMapping(value = "syslog/list", method = { RequestMethod.GET, RequestMethod.POST})
    Object getSyslogConfig(){
        return sysloger.getSyslogConf().getSyslogConfig();
    }

    @ResponseBody
    @RequestMapping(value = "syslogalarm/list", method = { RequestMethod.GET, RequestMethod.POST})
    Object getSyslogAlarmConfig(){
        return northContext.getSysloger().getSyslogConf().getSyslogAlarmConfig();
    }

    @ResponseBody
    @RequestMapping(value = "syslog/edit", method = { RequestMethod.POST})
    Object editSyslogConfig(@RequestBody Map<String, Object> syslogConf){
        //更新配置
        sysloger.getSyslogConf().setSyslogConfig(syslogConf);

        System.out.println(syslogConf);

        Object services = syslogConf.get("services");
        System.out.println(services.getClass());

        return syslogConf;
    }

    @ResponseBody
    @RequestMapping(value = "syslogalarm/edit", method = { RequestMethod.POST})
    Object editSyslogAlarmConfig(@RequestBody Map<String, Object> syslogAlarmConf){
        sysloger.getSyslogConf().setSyslogAlarmConfig(syslogAlarmConf);
        return "not complete.";
    }

}
