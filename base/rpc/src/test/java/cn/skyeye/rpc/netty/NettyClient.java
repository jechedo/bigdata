package cn.skyeye.rpc.netty;

import cn.skyeye.rpc.netty.buffer.FileSegmentManagedBuffer;
import cn.skyeye.rpc.netty.buffer.ManagedBuffer;
import cn.skyeye.rpc.netty.client.TransportClient;
import cn.skyeye.rpc.netty.client.TransportClientBootstrap;
import cn.skyeye.rpc.netty.client.TransportClientFactory;
import cn.skyeye.rpc.netty.protocol.OneWayMessage;
import cn.skyeye.rpc.netty.sasl.SaslClientBootstrap;
import cn.skyeye.rpc.netty.sasl.SecretKeyHolder;
import cn.skyeye.rpc.netty.server.OneForOneStreamManager;
import cn.skyeye.rpc.netty.util.MapConfigProvider;
import cn.skyeye.rpc.netty.util.TransportConf;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.DefaultFileRegion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/27 18:50
 */
public class NettyClient {

    public static void startClient() throws IOException, InterruptedException {
        HashMap<String, String> config = Maps.newHashMap();
        config.put("rpc.authenticate.enableSaslEncryption", String.valueOf(true));
        MapConfigProvider configProvider = new MapConfigProvider(config);
        TransportConf transportConf = new TransportConf("NIO", configProvider);

        SecretKeyHolder keyHolder = new SecretKeyHolder() {
            @Override
            public String getSaslUser(String appId) {
                return "client";
            }

            @Override
            public String getSecretKey(String appId) {
                return "secret";
            }
        };

        List<TransportClientBootstrap> clientBootstraps = new ArrayList<>();
        clientBootstraps.add(new SaslClientBootstrap(transportConf, "user", keyHolder));

        NettyClientRpcHandler rpcHandler = new NettyClientRpcHandler();
        TransportContext transportContext = new TransportContext(transportConf, rpcHandler);
        TransportClientFactory clientFactory = transportContext.createClientFactory(clientBootstraps);
        TransportClient transportClient = clientFactory.createClient("localhost", 8811);

        File file = new File("D:/demo/LICENSE.txt");
        ManagedBuffer fileSegmentManagedBuffer = new FileSegmentManagedBuffer(transportConf, file, 0, file.length());
        //transportClient.send(fileSegmentManagedBuffer.nioByteBuffer());

        final DefaultFileRegion msg = (DefaultFileRegion) fileSegmentManagedBuffer.convertToNetty();
        Channel channel = transportClient.getChannel();
        OneForOneStreamManager streamManager = (OneForOneStreamManager) rpcHandler.getStreamManager();

        ChannelFuture write = channel.writeAndFlush(new OneWayMessage(fileSegmentManagedBuffer));

        //long streamId = streamManager.registerStream("demo", Lists.newArrayList(fileSegmentManagedBuffer).iterator());
        write.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                future.channel().close();
                System.out.println("upload ok");
            }
        });


        try {
            while (true){
                Thread.sleep(1000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {
         startClient();
    }
}
