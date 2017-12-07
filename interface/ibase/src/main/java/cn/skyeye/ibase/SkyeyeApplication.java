package cn.skyeye.ibase;

import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/14 16:46
 */
@Api(value = "测试", tags = { "测试" })
@RestController
@SpringBootApplication
public class SkyeyeApplication {

    protected final Log logger = LogFactory.getLog(SkyeyeApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SkyeyeApplication.class, args);
    }

    @ApiOperation(value = "测试rest", notes = "rest")
    @ApiImplicitParam(paramType = "get", name = "name", value = "test say hello", required = false, dataType = "String")
    @RequestMapping(value = "/", method = { RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    Object index(String name){
        Map<String, Object> res = Maps.newHashMap();
        res.put(String.format("hello, %s", name), "Welcome to skyeye!");
        return res;
    }



}
