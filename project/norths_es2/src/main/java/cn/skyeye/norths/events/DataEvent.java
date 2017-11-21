package cn.skyeye.norths.events;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/21 14:59
 */
public class DataEvent {

    private Map<String, Object> record;

    public DataEvent() {
    }

    public Map<String, Object> getRecord() {
        return record;
    }

    public void setRecord(Map<String, Object> record) {
        this.record = record;
    }

    @Override
    public String toString() {
        return record == null ? null : record.toString();
    }
}
