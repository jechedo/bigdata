package cn.skyeye.aptrules.alarms;

import cn.skyeye.aptrules.ARUtils;
import cn.skyeye.common.json.Jsons;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Description:
 *   告警表单
 * @author LiXiaoCong
 * @version 2017/10/20 15:20
 */
public class Alarm {
    private Map<String, Object> entry;
    private String timestamp;
    private String id;

    private Alarm(String id, String timestamp, Map<String, Object> data){
        this.id = id;
        this.timestamp = timestamp;
        if(data != null){
            this.entry = Maps.newHashMap(data);
        }else {
            this.entry = Maps.newHashMap();
        }
    }

    Alarm(String id, Map<String, Object> record){
        this.entry = Maps.newHashMap();
        this.id = id;

        this.timestamp = ARUtils.nowTimeStr();
        entry.put("@timestamp", timestamp);
        entry.put("_origin", Jsons.obj2JsonString(record));
    }


    public void addAlarmKV(String key, Object value){
        if(key != null && value != null){
            this.entry.put(key, value);
        }
    }

    public String getId() {
        return id;
    }

    public  Object getAlarmValue(String key){
        return this.entry.get(key);
    }

    public <T> T getAlarmValue(String key, T defualtValue){
        Object o = this.entry.get(key);
        if(o == null) return defualtValue;
        return (T)o;
    }

    public Map<String, Object> getAlarm() {
        return this.entry;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Alarm alarm = (Alarm) o;

        return id.equals(alarm.id) ;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static Alarm newAlarmByData(String id, String timestamp, Map<String, Object> data){
        return new Alarm(id, timestamp, data);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Alarm{");
        sb.append("id='").append(id);
        sb.append(", entry=").append(entry).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
