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
import cn.skyeye.rpc.netty.transfers.TransferService;
import cn.skyeye.rpc.netty.transfers.blocks.BlockFetchingListener;
import cn.skyeye.rpc.netty.transfers.blocks.BlockId;
import cn.skyeye.rpc.netty.util.JavaUtils;
import cn.skyeye.rpc.netty.util.MapConfigProvider;
import cn.skyeye.rpc.netty.util.NodeInfo;
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


        sleep();

    }

    private static void sleep() {
        try {
            while (true){
                Thread.sleep(1000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        RpcContext rpcContext = RpcContext.get();
        TransferService demo = rpcContext.newTransferService("demo", "172.24.66.212", 8811, null, null);

        NodeInfo nodeInfo = new NodeInfo("localhost", "172.24.66.212", 9911);

        String file = "D:/demo/LICENSE.txt";
        File file1 = new File(file);
        BlockId.FileBlockId fileBlockId = new BlockId.FileBlockId(file, 0, file1.length());


        demo.fetchBlocks(nodeInfo, new String[]{fileBlockId.getName()}, new BlockFetchingListener(){
            @Override
            public void onBlockFetchSuccess(String blockId, ManagedBuffer data) {
                try {
                    System.out.println(blockId + ": \n " + JavaUtils.bytesToString(data.nioByteBuffer()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onBlockFetchFailure(String blockId, Throwable exception) {
                System.err.println(blockId + ": \n " + exception);
            }
        });

       // demo.sendJson(nodeInfo, Jsons.obj2JsonString("hello world"));

       // BlockId.FileBlockId fileBlockId = new BlockId.FileBlockId("D:/demo/upload.log", 0, file1.length());

        //demo.uploadBlock(nodeInfo, fileBlockId,
                //new FileSegmentManagedBuffer(rpcContext.newTransportConf("demo", Maps.newHashMap()), file1, 0, file1.length()));

        sleep();
    }
}
