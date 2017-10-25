package cn.skyeye.aptrules.alarms;

import cn.skyeye.common.Dates;
import com.google.common.collect.Maps;

import java.util.*;

public class AlarmLRUCache {
  
   private static final float   hashTableLoadFactor = 0.75f;

   private LinkedHashMap<String,Alarm> map;
   private LinkedHashMap<String, Long>   insertTimes;
   private int                  cacheSize;
   private long maxCacheTime = 24 * 60 * 60 * 1000L;

   public AlarmLRUCache(final int cacheSize) {
      this.cacheSize = cacheSize;
      int hashTableCapacity = (int)Math.ceil(cacheSize / hashTableLoadFactor) + 1;
      //当参数accessOrder为true时，即会按照访问顺序排序，最近访问的放在最前，最早访问的放在后面
      this.map = new LinkedHashMap<String,Alarm>(hashTableCapacity, hashTableLoadFactor, true) {

         private static final long serialVersionUID = 1;
         @Override
         protected boolean removeEldestEntry (Map.Entry<String,Alarm> eldest) {
            Long time = insertTimes.get(eldest.getKey());
            boolean status = (time == null ? false : ((System.currentTimeMillis() - time) > maxCacheTime));
            return (status || size() > AlarmLRUCache.this.cacheSize);
         }
      };
      this.insertTimes = Maps.newLinkedHashMap();
   }


   public synchronized Alarm get (String key) {
      Long time = insertTimes.get(key);
      if(time == null){
         return map.get(key);
      }else {
         //双重验证数据缓存是否超时
         if((System.currentTimeMillis() - time) > maxCacheTime){
            insertTimes.remove(key);
            map.remove(key);
            return null;
         }
      }
      return map.get(key);
   }


   public synchronized void put (String key, Alarm value) {
      insertTimes.put(key, Dates.getTodayTime());
      map.put (key, value);
   }


   public synchronized void clear() {
      map.clear();
      insertTimes.clear();
   }

   public synchronized int usedEntries() {
      return map.size();
   }

   public synchronized Collection<Map.Entry<String, Alarm>> getAll() {
      return new ArrayList<>(map.entrySet());
   }

   public synchronized void remove(String key){
      map.remove(key);
      insertTimes.remove(key);
   }

}