package cn.skyeye.norths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    @RequestMapping(value = "/", method = { RequestMethod.GET, RequestMethod.POST})
    String index(String name){
        return "Welcome to skyeye`s norths interface.";
    }
}
