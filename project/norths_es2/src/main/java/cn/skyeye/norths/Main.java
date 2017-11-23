package cn.skyeye.norths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 *
 *   由于数据源 和 处理器是不做绑定的
 *        当增加一个处理器时， 不会处理数据源以及抽取过的数据。
 *        日志格式？
 *        分批
 *
 * @author LiXiaoCong
 * @version 2017/11/22 9:57
 */
@RestController
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        NorthContext.get().start();
    }
    @RequestMapping(value = "/", method = { RequestMethod.GET, RequestMethod.POST})
    String index(String name){
        return "Welcome to skyeye`s norths interface.";
    }
}
