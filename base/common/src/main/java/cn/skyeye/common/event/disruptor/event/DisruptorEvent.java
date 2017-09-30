package cn.skyeye.common.event.disruptor.event;

import java.util.Date;

/**
 * Description:
 *      etl的单批数据事件
 * @author LiXiaoCong
 * @version 1.0
 * @date 2016/11/23 10:17
 */
public class DisruptorEvent {

    private EventEntry entry;

    public String getId(){
        return entry.getId();
    }

    public String getSource(){
        return entry.getSource();
    }

    public void setEntry(EventEntry entry) {
        this.entry = entry;
    }

    public int getBatchSize(){
        return entry.getBatchSize();
    }

    public Date getLatestDate(){
        return entry.getLatestDate();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DisruptorEvent{");
        sb.append("entry=").append(entry);
        sb.append('}');
        return sb.toString();
    }
}
