package cn.skyeye.rpc.netty.transfers.blocks;

import cn.skyeye.rpc.netty.buffer.FileSegmentManagedBuffer;
import cn.skyeye.rpc.netty.buffer.ManagedBuffer;
import cn.skyeye.rpc.netty.util.JavaUtils;
import cn.skyeye.rpc.netty.util.TransportConf;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/31 17:34
 */
public class LocalFileDataManager implements BlockDataManager {
    private final Logger logger = Logger.getLogger(LocalFileDataManager.class);

    private TransportConf conf;

    public LocalFileDataManager(TransportConf conf){
        this.conf = conf;
    }

    @Override
    public ManagedBuffer getBlockData(BlockId blockId) {
        BlockId.FileBlockId fileBlockId = blockId.asFileBlockId();
        ManagedBuffer res = null;
        if(fileBlockId != null){
            res = new FileSegmentManagedBuffer(conf, new File(fileBlockId.getFile()),
                    fileBlockId.getOffset(), fileBlockId.getLength());
        }
        return res;
    }

    @Override
    public boolean putBlockData(BlockId blockId, ManagedBuffer data) {
        BlockId.FileBlockId fileBlockId = blockId.asFileBlockId();
        boolean res = false;
        if(fileBlockId != null){
            try {
                byte[] buffer = JavaUtils.bufferToArray(data.nioByteBuffer());
                File file = new File(fileBlockId.getFile());
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                IOUtils.write(buffer, bufferedWriter);
                bufferedWriter.flush();
                fileWriter.flush();

                IOUtils.closeQuietly(bufferedWriter);
                IOUtils.closeQuietly(fileWriter);
                return true;
            } catch (IOException e) {
                logger.error(String.format("写文件%s失败。", fileBlockId), e);
            }
        }
        return res;
    }

    @Override
    public void releaseLock(BlockId blockId, long taskAttemptId) {

    }
}
