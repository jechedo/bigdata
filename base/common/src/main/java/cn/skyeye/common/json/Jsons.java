package cn.skyeye.common.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/4/11 13:19
 */
public class Jsons {
    public static <K, V> Map<K, V> toMap(String jsonStr) throws Exception{

        return JSON.parseObject(jsonStr,
                new TypeReference<Map<K, V>>() {});
    }

    public static <V> List<V> toList(String jsonStr) throws Exception{

        return JSON.parseObject(jsonStr,
                new TypeReference<List<V>>() {});
    }

    public static <V> Set<V> toSet(String jsonStr) throws Exception{

        return JSON.parseObject(jsonStr,
                new TypeReference<Set<V>>() {});
    }

    public static String obj2JsonString(Object obj){
        return JSON.toJSONString(obj);
    }

    public static Map<String, Object> str2Json(String str) {
        return (Map<String, Object>) JSON.parse(str);
    }

    private Jsons(){}
}
