package cn.skyeye.aptrules.exceptions;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/27 15:53
 */
public class IoCQueryException extends Exception {

    public IoCQueryException(Throwable cause){
        super(String.format("%s"));
    }

    public IoCQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
