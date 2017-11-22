package cn.skyeye.norths;

import com.google.common.collect.Maps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/22 9:57
 */
@RestController
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @RequestMapping(value = "/", method = { RequestMethod.POST})
    @ResponseBody
    Object index(@RequestBody Map<String, String> name){
        Map<String, Object> res = Maps.newHashMap();
        res.put(String.format("hello, %s", name.get("name")), "Welcome to skyeye!");
        return res;
    }
}
