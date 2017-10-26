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

    private Alarm(String timestamp, Map<String, Object> data){
        this.timestamp = timestamp;
        if(data != null){
            this.entry = Maps.newHashMap(data);
        }else {
            this.entry = Maps.newHashMap();
        }
    }

    Alarm(Map<String, Object> record){
        this.entry = Maps.newHashMap();

        this.timestamp = ARUtils.nowTimeStr();
        entry.put("@timestamp", timestamp);
        entry.put("_origin", Jsons.obj2JsonString(record));
    }


    public void addAlarmKV(String key, Object value){
        if(key != null && value != null){
            this.entry.put(key, value);
        }
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

    public static Alarm newAlarmByData(String timestamp, Map<String, Object> data){
        return new Alarm(timestamp, data);
    }

}
