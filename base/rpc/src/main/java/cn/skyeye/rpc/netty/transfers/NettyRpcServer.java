package cn.skyeye.rpc.netty.transfers;

import cn.skyeye.rpc.netty.buffer.ManagedBuffer;
import cn.skyeye.rpc.netty.buffer.NioManagedBuffer;
import cn.skyeye.rpc.netty.client.RpcResponseCallback;
import cn.skyeye.rpc.netty.client.TransportClient;
import cn.skyeye.rpc.netty.server.OneForOneStreamManager;
import cn.skyeye.rpc.netty.server.RpcHandler;
import cn.skyeye.rpc.netty.server.StreamManager;
import cn.skyeye.rpc.netty.transfers.blocks.BlockDataManager;
import cn.skyeye.rpc.netty.transfers.blocks.BlockId;
import cn.skyeye.rpc.netty.transfers.blocks.OpenBlocks;
import cn.skyeye.rpc.netty.transfers.blocks.UploadBlock;
import cn.skyeye.rpc.netty.transfers.exceptions.UnrecognizedBlockId;
import cn.skyeye.rpc.netty.transfers.messages.JsonMessage;
import cn.skyeye.rpc.netty.transfers.messages.JsonMessageManager;
import cn.skyeye.rpc.netty.transfers.messages.TransferMessage;
import cn.skyeye.rpc.netty.transfers.stream.StreamHandle;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/31 15:07
 */
public class NettyRpcServer extends RpcHandler {

    private final Logger logger = Logger.getLogger(NettyRpcServer.class);
    private final OneForOneStreamManager streamManager = new OneForOneStreamManager();

    private String appId;
    private BlockDataManager blockDataManager;
    private JsonMessageManager jsonMessageManager;

    public NettyRpcServer(String appId,
                          BlockDataManager blockDataManager,
                          JsonMessageManager jsonMessageManager){
        this.appId = appId;
        this.blockDataManager = blockDataManager;
        this.jsonMessageManager = jsonMessageManager;
    }

    @Override
    public void receive(TransportClient client,
                        ByteBuffer rpcMessage,
                        RpcResponseCallback callback) {

        TransferMessage message = TransferMessage.Decoder.fromByteBuffer(rpcMessage);
        logger.trace(String.format("Received request: %s", message));

        if(message instanceof UploadBlock){
            UploadBlock uploadBlock = (UploadBlock) message;
            doUpload(uploadBlock, callback);

        }else if(message instanceof OpenBlocks){
            OpenBlocks openBlocks = (OpenBlocks) message;
            doOpen(openBlocks, callback);

        }else if(message instanceof JsonMessage){
            JsonMessage jsonMessage = (JsonMessage) message;
            doJson(jsonMessage, callback);
        }
    }

    private void doJson(JsonMessage jsonMessage, RpcResponseCallback callback){
        byte[] result = jsonMessageManager.handleMessage(jsonMessage.jsonStr);
        if(result == null || result.length == 0) {
            callback.onSuccess(ByteBuffer.allocate(0));
        }else{
            //callback.onSuccess(ByteBuffer.wrap(result));
            ByteBuf buf = Unpooled.buffer(result.length);
            buf.writeBytes(result);
            callback.onSuccess(buf.nioBuffer());
        }
    }

    private void doOpen(OpenBlocks openBlocks, RpcResponseCallback callback) {
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
            logger.error(String.format("打开文件%s操作失败。", openBlocks), e);
            callback.onFailure(e);
        }
    }

    private void doUpload(UploadBlock uploadBlock, RpcResponseCallback callback) {
        NioManagedBuffer data = new NioManagedBuffer(ByteBuffer.wrap(uploadBlock.blockData));
        try {
            BlockId parse = BlockId.parse(uploadBlock.blockId);
            blockDataManager.putBlockData(parse, data);
            callback.onSuccess(ByteBuffer.allocate(0));
        } catch (UnrecognizedBlockId e) {
           logger.error(String.format("上传文件失败。", uploadBlock), e);
            callback.onFailure(e);
        }
    }

    @Override
    public StreamManager getStreamManager() {
        return streamManager;
    }
}
