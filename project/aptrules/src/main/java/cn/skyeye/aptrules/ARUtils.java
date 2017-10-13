package cn.skyeye.aptrules;

import com.google.common.base.Joiner;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/13 11:55
 */
public class ARUtils {
    private ARUtils(){}

    public static String concat(Object ... args){
        return Joiner.on(":").join(args);
    }

    public static String concat(String separator, Object ... args){
        return Joiner.on(",").join(args);
    }
}
