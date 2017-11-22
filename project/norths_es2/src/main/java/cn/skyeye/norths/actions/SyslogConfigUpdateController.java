package cn.skyeye.norths.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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

    @ResponseBody
    @RequestMapping(value = "syslog/list", method = { RequestMethod.GET, RequestMethod.POST})
    Object getSyslogConfig(){
        return "not complete.";
    }

    @ResponseBody
    @RequestMapping(value = "syslogalarm/list", method = { RequestMethod.GET, RequestMethod.POST})
    Object getSyslogAlarmConfig(){
        return "not complete.";
    }

    @ResponseBody
    @RequestMapping(value = "syslog/edit", method = { RequestMethod.POST})
    Object editSyslogConfig(){
        return "not complete.";
    }

    @ResponseBody
    @RequestMapping(value = "syslogalarm/edit", method = { RequestMethod.POST})
    Object editSyslogAlarmConfig(){
        return "not complete.";
    }


}
