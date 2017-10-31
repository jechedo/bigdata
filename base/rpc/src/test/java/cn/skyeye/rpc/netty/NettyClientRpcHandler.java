package cn.skyeye.rpc.netty;

import cn.skyeye.rpc.netty.client.RpcResponseCallback;
import cn.skyeye.rpc.netty.client.TransportClient;
import cn.skyeye.rpc.netty.server.OneForOneStreamManager;
import cn.skyeye.rpc.netty.server.RpcHandler;
import cn.skyeye.rpc.netty.server.StreamManager;
import cn.skyeye.rpc.netty.util.JavaUtils;

import java.nio.ByteBuffer;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/27 18:51
 */
public class NettyClientRpcHandler extends RpcHandler {

    private StreamManager streamManager = new OneForOneStreamManager();

    @Override
    public void receive(TransportClient client, ByteBuffer message, RpcResponseCallback callback) {
        System.err.println(JavaUtils.bytesToString(message));
    }

    @Override
    public StreamManager getStreamManager() {
        return streamManager;
    }

}
