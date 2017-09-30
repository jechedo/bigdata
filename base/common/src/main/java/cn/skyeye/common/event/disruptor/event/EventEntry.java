package cn.skyeye.common.event.disruptor.event;

import java.util.Date;

/**
 * 事件实体
 */
public class EventEntry{

    //事件类型ID
    private String id;
    //事件源
    private String source;
    //当前批次号
    private long batchNum;
    //当前批次记录条数
    private int batchSize;
    //当前批次的数据
    private Object batchData;

    private Date latestDate = new Date();

    public EventEntry(String id,
                      String source,
                      long batchNum,
                      int batchSize,
                      Object batchData) {
        this.id = id;
        this.source = source;
        this.batchNum = batchNum;
        this.batchSize = batchSize;
        this.batchData = batchData;
    }

    public String getId() {
       return id;
   }

   public String getSource() {
       return source;
   }

   public long getBatchNum() {
       return batchNum;
   }

    public <T> T getBatchData() {
        if(batchData == null) return null;
        return (T)batchData;
    }

    public <T> T getBatchData(T defaultValue) {
        if(batchData == null) return defaultValue;
        return (T)batchData;
    }

    public Date getLatestDate() {
        return latestDate;
    }

    public void setLatestDate(Date latestDate) {
        this.latestDate = latestDate;
    }

    /**
     * @param date   时间戳 精确到毫秒
     */
    public void setLatestDate(long date) {
        setLatestDate(new Date(date));
    }

    public int getBatchSize() {
        return batchSize;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EventEntry{");
        sb.append("id='").append(id).append('\'');
        sb.append(", source='").append(source).append('\'');
        sb.append(", batchNum=").append(batchNum);
        sb.append(", batchSize=").append(batchSize);
        sb.append(", latestDate=").append(latestDate);
        sb.append(", batchData=").append(batchData);
        sb.append('}');
        return sb.toString();
    }
}