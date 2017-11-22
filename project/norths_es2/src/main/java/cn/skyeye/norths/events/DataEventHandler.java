package cn.skyeye.norths.events;

import com.lmax.disruptor.EventHandler;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/21 15:46
 */
public abstract class DataEventHandler implements EventHandler<DataEvent> {

    protected final Logger logger = Logger.getLogger(DataEventHandler.class);
    protected AtomicLong totalEvent = new AtomicLong(0);
    protected AtomicBoolean endOfBatch = new AtomicBoolean(false);

    @Override
    public void onEvent(DataEvent event, long sequence, boolean endOfBatch) throws Exception {
        if(isAcceept(event)) {
            onEvent(event);
            this.endOfBatch.set(endOfBatch);
        }
        totalEvent.incrementAndGet();
    }

    public abstract void onEvent(DataEvent event);

    public abstract boolean isAcceept(DataEvent event);

    public void shutdown(long total){
        while (total > totalEvent.get()){
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {}
        }

        logger.info(String.format("数据总量为：%s, 处理数据量为：%s。", total, totalEvent.get()));
    }
}
