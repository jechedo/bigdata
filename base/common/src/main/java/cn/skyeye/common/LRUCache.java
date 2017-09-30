package cn.skyeye.common;

import com.google.common.collect.Maps;

import java.util.LinkedHashMap;
import java.util.Collection;  
import java.util.Map;  
import java.util.ArrayList;  
  
/** 
* An LRU cache, based on <code>LinkedHashMap</code>. 
* 
* <p> 
* This cache has a fixed maximum number of elements (<code>cacheSize</code>). 
* If the cache is full and another entry is added, the LRU (least recently used) entry is dropped. 
* 
* <p> 
* This class is thread-safe. All methods of this class are synchronized. 
* 
* <p> 
* Author: Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland<br> 
* Multi-licensed: EPL / LGPL / GPL / AL / BSD.
 * 超时时间可以理解为发呆时间  每次 获取缓存 会更新时间
*/  
public class LRUCache<K,V> {  
  
   private static final float   hashTableLoadFactor = 0.75f;

   private LinkedHashMap<K,V>   map;
   private LinkedHashMap<K, Long>   insertTimes;
   private int                  cacheSize;
   private long cacheOutTime = -1L;


   public LRUCache (final int cacheSize){
      this(cacheSize, -1L);
   }

   /**
   * Creates a new LRU cache.
   * @param cacheSize the maximum number of entries that will be kept in this cache.
    *@param cacheOutTime 缓存超时时间  单位：毫秒
   */
   public LRUCache (final int cacheSize, final long cacheOutTime) {
      this.cacheSize = cacheSize;
      int hashTableCapacity = (int)Math.ceil(cacheSize / hashTableLoadFactor) + 1;
      //当参数accessOrder为true时，即会按照访问顺序排序，最近访问的放在最前，最早访问的放在后面
      this.map = new LinkedHashMap<K,V>(hashTableCapacity, hashTableLoadFactor, true) {

         private static final long serialVersionUID = 1;
         @Override
         protected boolean removeEldestEntry (Map.Entry<K,V> eldest) {
            boolean status = false;
            if(cacheOutTime > 0) {
               Long aLong = insertTimes.get(eldest.getKey());

               status = (aLong == null ? false : ((System.currentTimeMillis() - aLong) >= cacheOutTime));
            }
            return (status || size() > LRUCache.this.cacheSize);
         }
      };

      this.insertTimes = Maps.newLinkedHashMap();
      this.cacheOutTime = cacheOutTime;
   }


   /**
   * Retrieves an entry from the cache.<br>
   * The retrieved entry becomes the MRU (most recently used) entry.
   * @param key the key whose associated value is to be returned.
   * @return    the value associated to this key, or null if no value with this key exists in the cache.
   */
   public synchronized V get (K key) {
      Long time = insertTimes.get(key);
      if(time == null){
         return map.get(key);
      }else {
         //双重验证数据缓存是否超时
         if((System.currentTimeMillis() - time) >= cacheOutTime){
            insertTimes.remove(key);
            map.remove(key);
            return null;
         }
      }

      insertTimes.put(key, System.currentTimeMillis());
      return map.get(key);
   }

   /**
   * Adds an entry to this cache.
   * The new entry becomes the MRU (most recently used) entry.
   * If an entry with the specified key already exists in the cache, it is replaced by the new entry.
   * If the cache is full, the LRU (least recently used) entry is removed from the cache.
   * @param key    the key with which the specified value is to be associated.
   * @param value  a value to be associated with the specified key.
   */
   public synchronized void put (K key, V value) {
      insertTimes.put(key, System.currentTimeMillis());
      map.put (key, value);
   }

   /**
   * Clears the cache.
   */
   public synchronized void clear() {
      map.clear();
      insertTimes.clear();
   }

   /**
   * Returns the number of used entries in the cache.
   * @return the number of entries currently in the cache.
   */
   public synchronized int usedEntries() {
      return map.size();
   }


   /**
   * Returns a <code>Collection</code> that contains a copy of all cache entries.
   * @return a <code>Collection</code> with a copy of the cache content.
   */
   public synchronized Collection<Map.Entry<K,V>> getAll() {
      return new ArrayList<Map.Entry<K,V>>(map.entrySet());
   }

   public synchronized void remove(K key){
      map.remove(key);
   }
  
}