package cn.skyeye.aptrules;

import com.google.common.base.Joiner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/13 11:55
 */
public class ARUtils {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private ARUtils(){}

    public static String concat(Object ... args){
        return Joiner.on(":").join(args);
    }

    public static String concatWithSeparator(String separator, Object ... args){
        return Joiner.on(",").join(args);
    }

    public static <K, T> T getValueByKeyInMap(Map<K, Object> map, K key){
        return getValueByKeyInMap(map, key, null);
    }

    public static <K, T> T getValueByKeyInMap(Map<K, Object> map, K key, T defaultValue){
        Object o = map.get(key);
        if(o == null) return defaultValue;
        return (T)o;
    }

    public static <K, T> T getValueOnceKeyExistInMap(Map<K, Object> map, K ... keys){
        return getValueOnceKeyExistInMapWithDefault(map, null, keys);
    }

    public static <K, T> T getValueOnceKeyExistInMapWithDefault(Map<K, Object> map, T defautValue, K ... keys){
        Object o;
        for(K key : keys){
            o = map.get(key);
            if(o != null){
                return (T)o;
            }
        }
        return defautValue;
    }

    public static String nowTimeStr(){
        return formatDate(new Date());
    }

    public static String formatTime(long milliseconds){
        return formatDate(new Date(milliseconds));
    }

    public static String formatDate(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
        return simpleDateFormat.format(date);
    }
}
