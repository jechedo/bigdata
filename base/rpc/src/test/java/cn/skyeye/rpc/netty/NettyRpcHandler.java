package cn.skyeye.rpc.netty;

import cn.skyeye.rpc.netty.client.RpcResponseCallback;
import cn.skyeye.rpc.netty.client.TransportClient;
import cn.skyeye.rpc.netty.server.RpcHandler;
import cn.skyeye.rpc.netty.server.StreamManager;

import java.nio.ByteBuffer;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/27 18:51
 */
public class NettyRpcHandler extends RpcHandler {

    @Override
    public void receive(TransportClient client, ByteBuffer message, RpcResponseCallback callback) {

    }

    @Override
    public StreamManager getStreamManager() {
        return null;
    }
}
