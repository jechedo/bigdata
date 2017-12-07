package cn.skyeye.rpc.netty.transfers.messages;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/1 10:04
 */
public class SimpleJsonMessageManager implements JsonMessageManager {

    @Override
    public byte[] handleMessage(String jsonMessage) {
        System.err.println("JsonMessage = " + jsonMessage);
        return null;
    }
}
