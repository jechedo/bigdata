package cn.skyeye.cascade.rpc.managers;

import cn.skyeye.rpc.netty.transfers.messages.JsonMessageManager;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/12 17:51
 */
public class JsonDataManager implements JsonMessageManager {

    @Override
    public byte[] handleMessage(String jsonMessage) {
        return new byte[0];
    }
}
