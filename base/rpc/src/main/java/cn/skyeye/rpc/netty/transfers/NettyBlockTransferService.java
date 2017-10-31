package cn.skyeye.rpc.netty.transfers;

import cn.skyeye.rpc.netty.RpcContext;
import cn.skyeye.rpc.netty.TransportContext;
import cn.skyeye.rpc.netty.buffer.ManagedBuffer;
import cn.skyeye.rpc.netty.client.RpcResponseCallback;
import cn.skyeye.rpc.netty.client.TransportClient;
import cn.skyeye.rpc.netty.client.TransportClientFactory;
import cn.skyeye.rpc.netty.server.TransportServer;
import cn.skyeye.rpc.netty.transfers.blocks.*;
import cn.skyeye.rpc.netty.util.JavaUtils;
import cn.skyeye.rpc.netty.util.NodeInfo;
import cn.skyeye.rpc.netty.util.TransportConf;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/31 15:55
 */
public class NettyBlockTransferService extends BlockTransferService {

    private String appId;
    private NodeInfo nodeInfo;

    private TransportContext transportContext;
    private TransportConf transportConf;
    private TransportServer transportServer;
    private TransportClientFactory transportClientFactory;

    public NettyBlockTransferService(String appId, NodeInfo nodeInfo){
        this.appId = appId;
        this.nodeInfo = nodeInfo;
        this.hostname = nodeInfo.getIp();
        this.port = nodeInfo.getPort();
    }

    @Override
    public void init(BlockDataManager blockDataManager) {
        NettyBlockRpcServer rpcHandler = new NettyBlockRpcServer(appId, blockDataManager);
        RpcContext rpcContext = RpcContext.get();
        this.transportContext = rpcContext.newTransportContext(appId, Maps.newHashMap(), rpcHandler);
        this.transportConf = transportContext.getConf();
        this.transportServer = rpcContext.newTransportServer(hostname, port, transportContext);
        this.transportClientFactory = rpcContext.newTransportClientFactory(appId, transportContext);

        logger.info(String.format("Server created on %s:%s", hostname, port));
    }

    @Override
    public void close() {
        if (transportServer != null) {
            transportServer.close();
        }
        if (transportClientFactory != null) {
            transportClientFactory.close();
        }
    }

    @Override
    public void fetchBlocks(NodeInfo nodeInfo,  String[] blockIds, BlockFetchingListener blockFetchingListener) {
        logger.trace(String.format("Fetch blocks from %s:%s (nodeId %s)",
                nodeInfo.getIp(), nodeInfo.getPort(), nodeInfo.getHostname()));
        try {
            RetryingBlockFetcher.BlockFetchStarter blockFetchStarter = new RetryingBlockFetcher.BlockFetchStarter() {
                @Override
                public void createAndStart(String[] blockIds, BlockFetchingListener listener) throws IOException, InterruptedException {
                    TransportClient client = transportClientFactory.createClient(nodeInfo.getIp(), nodeInfo.getPort());
                    new OneForOneBlockFetcher(client, appId, nodeInfo.getHostname(), blockIds, listener,
                            transportConf, null).start();
                }
            };

            int maxRetries = transportConf.maxIORetries();
            if (maxRetries > 0) {
                new RetryingBlockFetcher(transportConf, blockFetchStarter, blockIds, blockFetchingListener).start();
            } else {
                blockFetchStarter.createAndStart(blockIds, blockFetchingListener);
            }
        }catch (Exception e){
            logger.error(String.format("Exception while beginning fetchBlocks: %s", Lists.newArrayList(blockIds)), e);
        }
    }

    @Override
    public void uploadBlock(NodeInfo nodeInfo, BlockId blockId, ManagedBuffer managedBuffer) {
        try {
            TransportClient client = transportClientFactory.createClient(nodeInfo.getIp(), nodeInfo.getPort());
            byte[] data = JavaUtils.bufferToArray(managedBuffer.nioByteBuffer());
            client.sendRpc(new UploadBlock(appId, nodeInfo.getHostname(), blockId.getName(), data).toByteBuffer(),
                    new RpcResponseCallback() {
                        @Override
                        public void onSuccess(ByteBuffer response) {
                            logger.trace(String.format("Successfully uploaded block %s", blockId));
                        }
                        @Override
                        public void onFailure(Throwable e) {
                            logger.error(String.format("Error while uploading block %s", blockId), e);
                        }
                    });
            //client.close();
        } catch (Exception e) {
            logger.error(String.format("Error while uploading block %s", blockId), e);
        }
    }
}
