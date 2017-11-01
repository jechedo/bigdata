package cn.skyeye.rpc.netty.transfers;

import cn.skyeye.rpc.netty.buffer.ManagedBuffer;
import cn.skyeye.rpc.netty.transfers.blocks.BlockDataManager;
import cn.skyeye.rpc.netty.transfers.blocks.BlockFetchingListener;
import cn.skyeye.rpc.netty.transfers.blocks.BlockId;
import cn.skyeye.rpc.netty.transfers.messages.JsonMessageManager;
import cn.skyeye.rpc.netty.util.NodeInfo;
import org.apache.log4j.Logger;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/31 14:57
 */
public abstract class TransferService {

    protected final Logger logger = Logger.getLogger(TransferService.class);

    protected int port;
    protected String hostname;

    public abstract void init(BlockDataManager blockDataManager, JsonMessageManager jsonMessageManager);
    public abstract void close();

    public abstract void fetchBlocks(NodeInfo nodeInfo,
                                     String[] blockIds,
                                     BlockFetchingListener blockFetchingListener);

    public abstract void uploadBlock(NodeInfo nodeInfo,
                                     BlockId blockId,
                                     ManagedBuffer managedBuffer);

    public abstract void sendJson(NodeInfo nodeInfo, String jsonStr);


}
