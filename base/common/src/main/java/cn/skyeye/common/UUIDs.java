package cn.skyeye.common;

import java.util.UUID;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/7/28 16:06
 */
public class UUIDs {

    private UUIDs(){}

    public static String uuidFromBytes(byte[] bytes){
        return UUID.nameUUIDFromBytes(bytes).toString();
    }

    public static String uuidFromBytesWithNoSeparator(byte[] bytes){
        return uuidFromBytes(bytes).replaceAll("-", "");
    }


}
