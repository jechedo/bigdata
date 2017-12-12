package cn.skyeye.cascade.rpc.managers;

import cn.skyeye.rpc.netty.buffer.ManagedBuffer;
import cn.skyeye.rpc.netty.transfers.blocks.BlockDataManager;
import cn.skyeye.rpc.netty.transfers.blocks.BlockId;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/12 17:48
 */
public class FileDataManager implements BlockDataManager {

    @Override
    public ManagedBuffer getBlockData(BlockId blockId) {
        return null;
    }

    @Override
    public boolean putBlockData(BlockId blockId, ManagedBuffer data) {
        return false;
    }

    @Override
    public void releaseLock(BlockId blockId, long taskAttemptId) {

    }
}
