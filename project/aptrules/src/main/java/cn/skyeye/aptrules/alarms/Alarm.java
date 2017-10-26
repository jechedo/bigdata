package cn.skyeye.aptrules.alarms;

import cn.skyeye.aptrules.ARUtils;
import cn.skyeye.aptrules.Record;
import cn.skyeye.common.json.Jsons;

import java.util.Map;

/**
 * Description:
 *   告警表单
 * @author LiXiaoCong
 * @version 2017/10/20 15:20
 */
public class Alarm extends Record {
    private String timestamp;
    private String id;

    public Alarm(String id, String timestamp, Map<String, Object> data){
        super(data);
        this.id = id;
        this.timestamp = timestamp;
    }

    Alarm(String id, Map<String, Object> record){
        super(null);
        this.id = id;
        this.timestamp = ARUtils.nowTimeStr();
        this.data.put("@timestamp", timestamp);
        this.data.put("_origin", Jsons.obj2JsonString(record));
    }


    public void addAlarmKV(String key, Object value){
        if(key != null && value != null){
            this.data.put(key, value);
        }
    }

    public String getId() {
        return id;
    }

    public  Object getAlarmValue(String key){
        return this.data.get(key);
    }

    public <T> T getAlarmValue(String key, T defualtValue){
        Object o = this.data.get(key);
        if(o == null) return defualtValue;
        return (T)o;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Alarm{");
        sb.append("id='").append(id);
        sb.append(", data=").append(data).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
