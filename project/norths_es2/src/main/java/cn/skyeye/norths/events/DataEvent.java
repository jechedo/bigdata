package cn.skyeye.norths.events;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/21 14:59
 */
public class DataEvent {

    private String source;
    private Map<String, Object> record;

    public DataEvent() {
    }

    public Map<String, Object> getRecord() {
        return Maps.newHashMap(record);
    }

    public void setRecord(Map<String, Object> record) {
        this.record = record;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataEvent{");
        sb.append("source='").append(source).append('\'');
        sb.append(", record=").append(record);
        sb.append('}');
        return sb.toString();
    }
}
