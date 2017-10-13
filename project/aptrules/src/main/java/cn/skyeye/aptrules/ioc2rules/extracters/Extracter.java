package cn.skyeye.aptrules.ioc2rules.extracters;

import java.util.Map;

/**
 * Description:
 *    ioc解析器
 * @author LiXiaoCong
 * @version 2017/10/13 10:53
 */
public abstract class Extracter {
    public abstract void extract(Map<String, Object> ioc);
}
