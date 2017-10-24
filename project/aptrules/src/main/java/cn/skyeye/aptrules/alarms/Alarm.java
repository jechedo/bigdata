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
    private String ruleKey;
    private Map<String, Object> entry;
    private String timestamp;

    Alarm(String ruleKey, Map<String, Object> record){
        this.ruleKey = ruleKey;
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

    public String getRuleKey() {
        return ruleKey;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Alarm alarm = (Alarm) o;
        return ruleKey.equals(alarm.ruleKey);
    }

    @Override
    public int hashCode() {
        return ruleKey.hashCode();
    }
}
