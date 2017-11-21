package cn.skyeye.norths.events;

import com.lmax.disruptor.EventHandler;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/21 15:46
 */
public abstract class DataEventHandler implements EventHandler<DataEvent> {

    @Override
    public void onEvent(DataEvent event, long sequence, boolean endOfBatch) throws Exception {
        onEvent(event, endOfBatch);
    }

    public abstract void onEvent(DataEvent event,  boolean endOfBatch);

    public abstract void shutdown(long total);
}
