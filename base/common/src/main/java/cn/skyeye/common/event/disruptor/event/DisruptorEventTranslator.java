package cn.skyeye.common.event.disruptor.event;

import com.google.common.collect.Maps;
import com.lmax.disruptor.EventTranslatorOneArg;

import java.util.Map;

/**
 * Description:
 *       负责事件翻译  以及 事件信息统计
 * @author LiXiaoCong
 * @version 1.0
 * @date 2016/12/7 10:57
 */
public abstract class DisruptorEventTranslator
        implements EventTranslatorOneArg<DisruptorEvent, EventEntry> {

    protected Map<String, Long> currentBatchMap;
    protected Map<String, Long> totalBatchSizeMap;

    public DisruptorEventTranslator(){
        this.currentBatchMap = Maps.newConcurrentMap();
        this.totalBatchSizeMap =  Maps.newConcurrentMap();
    }

    public void translateTo(DisruptorEvent event, long sequence, EventEntry eventEntry) {
        event.setEntry(eventEntry);
        updateBatchStatus(eventEntry);
    }

    protected void updateBatchStatus(EventEntry eventEntry){
        String eventId  = eventEntry.getId();
        currentBatchMap.put(eventId, eventEntry.getBatchNum());

        Long batchSize =  totalBatchSizeMap.get(eventId);
        if(batchSize == null){
            batchSize = Long.valueOf(eventEntry.getBatchSize());
        }else{
            batchSize += Long.valueOf(eventEntry.getBatchSize());
        }
        totalBatchSizeMap.put(eventId, batchSize);
    }


    public long getCurrentBatchNumById(String eventId){
        Long currentBatchNum = currentBatchMap.get(eventId);
        return currentBatchNum == null ? 0 : currentBatchNum;
    }

    public long getTotalBatchSizeById(String eventId){
        Long totalBatchSize = totalBatchSizeMap.get(eventId);
        return totalBatchSize == null ? 0 : totalBatchSize;
    }

    public Map<String, Long> getCurrentBatchMap() {
        return currentBatchMap;
    }

    public Map<String, Long> getTotalBatchSizeMap() {
        return totalBatchSizeMap;
    }

}
