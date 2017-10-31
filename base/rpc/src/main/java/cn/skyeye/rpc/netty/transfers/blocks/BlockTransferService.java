package cn.skyeye.rpc.netty.transfers.blocks;

import cn.skyeye.rpc.netty.buffer.ManagedBuffer;
import cn.skyeye.rpc.netty.util.NodeInfo;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/31 14:57
 */
public abstract class BlockTransferService {

    protected final Logger logger = Logger.getLogger(BlockTransferService.class);

    protected int port;
    protected String hostname;

    public abstract void init(BlockDataManager blockDataManager);
    public abstract void close();

    public abstract void fetchBlocks(NodeInfo nodeInfo,
                                     List<BlockId> blockIds,
                                     BlockFetchingListener blockFetchingListener);

    public abstract void uploadBlock(NodeInfo nodeInfo,
                                     BlockId blockId,
                                     ManagedBuffer managedBuffer);


}
