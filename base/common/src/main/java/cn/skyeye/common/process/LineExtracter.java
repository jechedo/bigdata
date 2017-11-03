package cn.skyeye.common.process;

/**
 * Description:
 *
 * 进程输出信息行级别 解析接口
 *
 * @author LiXiaoCong
 * @version 1.0
 * @date 2016/10/24 14:18
 */
public interface LineExtracter {
    void extract(boolean isError, String line);
}
