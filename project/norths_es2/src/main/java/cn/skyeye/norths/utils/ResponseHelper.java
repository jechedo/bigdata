package cn.skyeye.norths.utils;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Description:
 *   用于封装响应请求的结果
 * @author LiXiaoCong
 * @version 2017/11/22 10:34
 */
public class ResponseHelper {
    private ResponseHelper(){}

    public static Map<String, Object> success(){
        return successBase();
    }

    public static Map<String, Object> success(Object data){
        Map<String, Object> res = successBase();
        res.put("data", data);
        return res;
    }

    public static Map<String, Object> success(long total, Object record){
        Map<String, Object> data = Maps.newHashMap();
        data.put("total", total);
        data.put("record", record);
        return success(data);
    }

    private static Map<String, Object> successBase(){
        Map<String, Object> res = Maps.newHashMap();
        res.put("status", 200);
        res.put("message", "ok");
        return res;
    }
}
