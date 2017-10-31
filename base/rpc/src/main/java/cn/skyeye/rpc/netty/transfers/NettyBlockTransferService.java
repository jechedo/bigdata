package cn.skyeye.rpc.netty.transfers;

import cn.skyeye.rpc.netty.RpcContext;
import cn.skyeye.rpc.netty.TransportContext;
import cn.skyeye.rpc.netty.buffer.ManagedBuffer;
import cn.skyeye.rpc.netty.client.TransportClientFactory;
import cn.skyeye.rpc.netty.server.TransportServer;
import cn.skyeye.rpc.netty.transfers.blocks.BlockDataManager;
import cn.skyeye.rpc.netty.transfers.blocks.BlockFetchingListener;
import cn.skyeye.rpc.netty.transfers.blocks.BlockId;
import cn.skyeye.rpc.netty.transfers.blocks.BlockTransferService;
import cn.skyeye.rpc.netty.util.NodeInfo;
import com.google.common.collect.Maps;

import java.util.List;

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
    public void fetchBlocks(NodeInfo nodeInfo, List<BlockId> blockIds, BlockFetchingListener blockFetchingListener) {

    }

    @Override
    public void uploadBlock(NodeInfo nodeInfo, BlockId blockId, ManagedBuffer managedBuffer) {

    }
}
