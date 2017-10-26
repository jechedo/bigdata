package cn.skyeye.aptrules.alarms.stores;

import cn.skyeye.aptrules.alarms.Alarm;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/25 18:54
 */
public abstract class AlarmStore {

    protected final Logger logger = Logger.getLogger(AlarmStore.class);

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

    public abstract List<Alarm> queryAlarmsInStore(String conditions, int maxSize);

    public abstract void storeAlarm(Alarm alarm);

    public abstract boolean exist(String alarmId);

    public abstract void updateAlarms(List<Alarm> assets);

    //public abstract String createConditions(String[] fields, Object[] fieldValues);
}
