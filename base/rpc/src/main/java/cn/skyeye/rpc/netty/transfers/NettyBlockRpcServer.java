package cn.skyeye.rpc.netty.transfers;

import cn.skyeye.rpc.netty.buffer.ManagedBuffer;
import cn.skyeye.rpc.netty.buffer.NioManagedBuffer;
import cn.skyeye.rpc.netty.client.RpcResponseCallback;
import cn.skyeye.rpc.netty.client.TransportClient;
import cn.skyeye.rpc.netty.server.OneForOneStreamManager;
import cn.skyeye.rpc.netty.server.RpcHandler;
import cn.skyeye.rpc.netty.server.StreamManager;
import cn.skyeye.rpc.netty.transfers.blocks.*;
import cn.skyeye.rpc.netty.transfers.exceptions.UnrecognizedBlockId;
import cn.skyeye.rpc.netty.transfers.stream.StreamHandle;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/31 15:07
 */
public class NettyBlockRpcServer extends RpcHandler {

    private final Logger logger = Logger.getLogger(NettyBlockRpcServer.class);
    private final OneForOneStreamManager streamManager = new OneForOneStreamManager();

    private String appId;
    private BlockDataManager blockDataManager;

    public NettyBlockRpcServer(String appId, BlockDataManager blockDataManager){
        this.appId = appId;
        this.blockDataManager = blockDataManager;
    }

    @Override
    public void receive(TransportClient client,
                        ByteBuffer rpcMessage,
                        RpcResponseCallback callback) {

        BlockTransferMessage message = BlockTransferMessage.Decoder.fromByteBuffer(rpcMessage);
        logger.trace(String.format("Received request: %s", message));

        if(message instanceof UploadBlock){
            UploadBlock uploadBlock = (UploadBlock) message;
            NioManagedBuffer data = new NioManagedBuffer(ByteBuffer.wrap(uploadBlock.blockData));
            try {
                BlockId parse = BlockId.parse(uploadBlock.blockId);
                blockDataManager.putBlockData(parse, data);
                callback.onSuccess(ByteBuffer.allocate(0));
            } catch (UnrecognizedBlockId e) {
               logger.error(String.format("上传文件失败。", message), e);
                callback.onFailure(e);
            }
        }else if(message instanceof OpenBlocks){
            OpenBlocks openBlocks = (OpenBlocks) message;
            String[] blockIds = openBlocks.blockIds;
            int blocksNum = blockIds.length;
            try {
                List<ManagedBuffer> blockDatas = Lists.newArrayListWithCapacity(blocksNum);
                ManagedBuffer blockData;
                for(int i = 0; i < blocksNum; i++){
                    blockData = blockDataManager.getBlockData(BlockId.parse(blockIds[i]));
                    blockDatas.add(blockData);
                }
                long streamId = streamManager.registerStream(appId, blockDatas.iterator());
                logger.trace(String.format("Registered streamId %s with %s buffers", streamId, blocksNum));

                callback.onSuccess(new StreamHandle(streamId, blocksNum).toByteBuffer());
            } catch (UnrecognizedBlockId e) {
                logger.error(String.format("打开文件%s操作失败。", message), e);
                callback.onFailure(e);
            }
        }
    }

    @Override
    public StreamManager getStreamManager() {
        return streamManager;
    }
}
