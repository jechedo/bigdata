package cn.skyeye.rpc.netty;

import cn.skyeye.rpc.netty.client.RpcResponseCallback;
import cn.skyeye.rpc.netty.client.TransportClient;
import cn.skyeye.rpc.netty.server.OneForOneStreamManager;
import cn.skyeye.rpc.netty.server.RpcHandler;
import cn.skyeye.rpc.netty.server.StreamManager;
import cn.skyeye.rpc.netty.util.JavaUtils;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/27 18:51
 */
public class NettyServerRpcHandler extends RpcHandler {

    private StreamManager streamManager = new OneForOneStreamManager();

    @Override
    public void receive(TransportClient client, ByteBuffer message, RpcResponseCallback callback) {
        System.out.println("-------" + JavaUtils.bytesToString(message));
        client.send(JavaUtils.stringToBytes("hello server i`m Server"));

    }

    @Override
    public StreamManager getStreamManager() {

        System.out.println("-------........");
        return streamManager;
    }

    @Override
    public void exceptionCaught(Throwable cause, TransportClient client) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) client.getSocketAddress();
        System.err.println(cause);
        System.err.println(inetSocketAddress);
    }
}
