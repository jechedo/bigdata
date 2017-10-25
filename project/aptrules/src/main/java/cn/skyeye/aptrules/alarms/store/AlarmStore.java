package cn.skyeye.aptrules.alarms.store;

import cn.skyeye.aptrules.alarms.Alarm;

import java.util.List;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/25 18:54
 */
public abstract class AlarmStore {

    private AlarmLRUCache memoryCache;

    public AlarmStore(){
        this.memoryCache = new AlarmLRUCache(100000);
    }

    public void addCache(String key, Alarm alarm){
        this.memoryCache.put(key, alarm);
    }

    public Alarm getAlarmInCache(String key) {
        return this.memoryCache.get(key);
    }

    public Alarm getAlarm(String key){
        Alarm alarm = this.memoryCache.get(key);
        if(alarm == null){
            List<Alarm> alarmsInStore = getAlarmsInStore(key, 1);
            if(alarmsInStore != null && !alarmsInStore.isEmpty()){
                alarm = alarmsInStore.get(0);
            }
        }
        return alarm;
    }

    public abstract List<Alarm> getAlarmsInStore(String key, int maxSize);
}
