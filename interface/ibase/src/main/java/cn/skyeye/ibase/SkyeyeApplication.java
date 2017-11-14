package cn.skyeye.ibase;

import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/14 16:46
 */
@RestController
@SpringBootApplication
public class SkyeyeApplication {

    protected final Log logger = LogFactory.getLog(SkyeyeApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SkyeyeApplication.class, args);
    }

    @RequestMapping(value = "/")
    @ResponseBody
    Object index(){
        Map<String, Object> res = Maps.newHashMap();
        res.put("hello", "Welcome to skyeye!");
        return res;
    }

}
