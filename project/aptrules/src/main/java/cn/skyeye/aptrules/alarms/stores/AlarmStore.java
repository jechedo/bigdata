package cn.skyeye.aptrules.alarms.stores;

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

    public Alarm getAlarmInCache(String conditions) {
        return this.memoryCache.get(conditions);
    }

    public Alarm getAlarm(String conditions){
        Alarm alarm = this.memoryCache.get(conditions);
        if(alarm == null){
            List<Alarm> alarmsInStore = getAlarmsInStore(conditions, 1);
            if(alarmsInStore != null && !alarmsInStore.isEmpty()){
                alarm = alarmsInStore.get(0);
                memoryCache.put(conditions, alarm);
            }
        }
        return alarm;
    }

    public abstract List<Alarm> getAlarmsInStore(String conditions, int maxSize);

    public abstract void storeAlarm(Alarm alarm);

    //public abstract String createConditions(String[] fields, Object[] fieldValues);
}
