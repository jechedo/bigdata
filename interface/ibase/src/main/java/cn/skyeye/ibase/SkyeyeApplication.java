package cn.skyeye.ibase;

import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(value = "/", method = { RequestMethod.POST})
    @ResponseBody
    Object index(@RequestBody Name name){
        Map<String, Object> res = Maps.newHashMap();
        res.put(String.format("hello, %s", name), "Welcome to skyeye!");
        return res;
    }



}
