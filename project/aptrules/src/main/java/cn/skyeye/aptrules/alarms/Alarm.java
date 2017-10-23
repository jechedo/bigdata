package cn.skyeye.aptrules.alarms;

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

    Alarm(){
        this.entry = Maps.newHashMap();
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
}
