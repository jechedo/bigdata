package cn.skyeye.rpc.netty.transfers.messages;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/1 9:58
 */
public interface JsonMessageManager {
    void handleMessage(String jsonMessage);
}
