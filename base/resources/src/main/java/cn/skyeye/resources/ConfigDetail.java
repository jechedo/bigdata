package cn.skyeye.resources;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Description:
 *
 *      配置细节
 *
 * @author LiXiaoCong
 * @version 1.0
 * @date 2016/10/21 15:03
 */
public class ConfigDetail implements Serializable{

    protected Map<String, String> configMap;

    public ConfigDetail(){
        this.configMap = Maps.newConcurrentMap();
    }

    public ConfigDetail(Map<String, String> configMap){
        this.configMap = Maps.newConcurrentMap();
       if(configMap != null){
           this.configMap.putAll(configMap);
       }
    }


    public void addConfig(String key, String value){
        if(!Strings.isNullOrEmpty(key)
                && !Strings.isNullOrEmpty(value))
            configMap.put(key, value);
    }

    public void addConfigIfMiss(String key, String value){

            if(!configMap.containsKey(key))
                configMap.put(key, value);
    }

    public String getConfigItemValue(String itemName){
        return configMap.get(itemName);
    }

    public String getConfigItemValue(String itemName, String defaultVal){
        String itemValue = getConfigItemValue(itemName);
        if(Strings.isNullOrEmpty(itemValue)) itemValue = defaultVal;
        return itemValue == null ? itemValue : itemValue.trim();
    }

    public Set<String> getConfigItemSet(String itemName){
        return getConfigItemSet(itemName, ",");
    }

    public Set<String> getConfigItemSet(String itemName, String splitRegex){
        String itemValue = getConfigItemValue(itemName);
        if(!Strings.isNullOrEmpty(itemValue)){
               return Sets.newHashSet(itemValue.split(splitRegex));
        }
        return Sets.newHashSet();
    }

    public List<String> getConfigItemList(String itemName){
        return getConfigItemList(itemName, ",");
    }

    public List<String> getConfigItemList(String itemName, String splitRegex){
        String itemValue = getConfigItemValue(itemName);
        if(!Strings.isNullOrEmpty(itemValue)){
               return Lists.newArrayList(itemValue.split(splitRegex));
        }
        return Lists.newArrayList();
    }

    public  Map<String, String> getConfigItemMap(String itemName, String mapSplitRegex){
        return getConfigItemMap(itemName,  ",", mapSplitRegex);
    }

    public  Map<String, String> getConfigItemMapWithEmpty(String itemName, String mapSplitRegex){
        return getConfigItemMapWithEmpty(itemName,  ",", mapSplitRegex);
    }

    public  Map<String, String> getConfigItemMap(String itemName,
                                                 String arraySplitRegex, String mapSplitRegex){

        HashMap<String, String> map = Maps.newHashMap();

        String itemValue = getConfigItemValue(itemName);
        if(!Strings.isNullOrEmpty(itemValue)){
            String[] split = itemValue.split(arraySplitRegex);
             int length = split.length;
            if(length > 0){
                String[] kv;
                for(int i = 0; i < length; i++){
                    kv = split[i].split(mapSplitRegex);
                    if(kv.length == 2){
                        map.put(kv[0], kv[1]);
                    }
                }
            }
        }
        return map;
    }

    public  Map<String, String> getConfigItemMapWithEmpty(
            String itemName, String arraySplitRegex, String mapSplitRegex){

        HashMap<String, String> map = Maps.newHashMap();

        String itemValue = getConfigItemValue(itemName);
        if(!Strings.isNullOrEmpty(itemValue)){
            String[] split = itemValue.split(arraySplitRegex);
             int length = split.length;
            if(length > 0){
                String[] kv;
                for(int i = 0; i < length; i++){
                    kv = split[i].split(mapSplitRegex);
                    if(kv.length == 1){
                        map.put(kv[0], null);
                    }else if(kv.length == 2){
                        map.put(kv[0], kv[1]);
                    }
                }
            }
        }
        return map;
    }

    public HashMultimap<String, String> getConfigItemMultiMap(String itemName, String mapSplitRegex){
        return getConfigItemMultiMap(itemName,  ",", mapSplitRegex);
    }

    public HashMultimap<String, String> getConfigItemMultiMapWithEmpty(String itemName, String mapSplitRegex){
        return getConfigItemMultiMapWithEmpty(itemName, ",", mapSplitRegex);
    }

    public HashMultimap<String, String> getConfigItemMultiMap(String itemName,
                                                 String arraySplitRegex, String mapSplitRegex){

        HashMultimap<String, String> map = HashMultimap.create();

        String itemValue = getConfigItemValue(itemName);
        if(Strings.isNullOrEmpty(itemValue)) return map;

        String[] split = itemValue.split(arraySplitRegex);
        String[] kv;
        for(int i = 0; i < split.length; i++){
            kv = split[i].split(mapSplitRegex);
            if(kv.length == 2){
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }

    public HashMultimap<String, String> getConfigItemMultiMapWithEmpty
            (String itemName, String arraySplitRegex, String mapSplitRegex){

        HashMultimap<String, String> map = HashMultimap.create();

        String itemValue = getConfigItemValue(itemName);
        if(Strings.isNullOrEmpty(itemValue)) return map;

        String[] split = itemValue.split(arraySplitRegex);
        String[] kv;
        for(int i = 0; i < split.length; i++){
            kv = split[i].split(mapSplitRegex);
            if(kv.length == 1){
                map.put(kv[0], null);
            }else if(kv.length == 2){
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }

    public Boolean getConfigItemBoolean(String key){
        return getConfigItemBoolean(key,null);
    }

    public Boolean getConfigItemBoolean(String key , Boolean defaultValue){

        String tmp = getConfigItemValue(key);
        if(!Strings.isNullOrEmpty(tmp)){
            if("true".equalsIgnoreCase(tmp)){
                return  true;
            }else if("false".equalsIgnoreCase(tmp)){
                return false;
            }
        }
        return defaultValue;
    }

    public Integer getConfigItemInteger(String key){
        return getConfigItemInteger(key,null);
    }

    public Integer getConfigItemInteger(String key , Integer defaultValue){

        String tmp = getConfigItemValue(key);
        if(!Strings.isNullOrEmpty(tmp)){
            return Integer.parseInt(tmp);
        }

        return defaultValue;
    }

    public Double  getConfigItemDouble(String key){
        return getConfigItemDouble(key,null);
    }
    public Double  getConfigItemDouble(String key , Double defaultValue){

        String tmp = getConfigItemValue(key , null);
        if(!Strings.isNullOrEmpty(tmp)){
           return  Double.parseDouble(tmp);
        }
        return defaultValue;
    }

    public Long getConfigItemLong(String key){
        return getConfigItemLong(key,null);
    }
    public Long getConfigItemLong(String key , Long defaultValue){

        String tmp = getConfigItemValue(key);
        if(!Strings.isNullOrEmpty(tmp)){
            return Long.parseLong(tmp);
        }
        return defaultValue;
    }

    public Float getConfigItemFloat(String key){
        return getConfigItemFloat(key,null);
    }
    public Float getConfigItemFloat(String key , Float defaultValue){

        String tmp = getConfigItemValue(key);
        if(!Strings.isNullOrEmpty(tmp)){
            return Float.parseFloat(tmp);
        }
        return defaultValue;
    }

    public Short getConfigItemShort(String key){
        return getConfigItemShort(key,null);
    }
    public Short getConfigItemShort(String key , Short defaultValue){

        String tmp = getConfigItemValue(key);
        if(!Strings.isNullOrEmpty(tmp)){
            return Short.parseShort(tmp);
        }
        return defaultValue;
    }

    public Byte getConfigItemByte(String key){
        return getConfigItemByte(key,null);
    }
    public Byte getConfigItemByte(String key , Byte defaultValue){

        String tmp = getConfigItemValue(key);
        if(!Strings.isNullOrEmpty(tmp)){
            return Byte.parseByte(tmp);
        }
        return defaultValue;
    }

    public Map<String, String> getConfigMap() {
        return Maps.newHashMap(configMap);
    }

    /**
     * 字段前缀会被删除
     *
     * @param prefix
     * @return
     */
    public Map<String, String> getConfigMap(String prefix) {
        Map<String, String> map = Maps.newHashMap();
        for(String field : configMap.keySet()){
            if(field.startsWith(prefix)){
                map.put(field.replace(prefix, ""), configMap.get(field));
            }
        }
        return map;
    }

    /**
     * 字段会保留前缀
     * @param prefix
     * @return
     */
    public Map<String, String> getConfigMapWithPrefix(String prefix) {
        Map<String, String> map = Maps.newHashMap();
        for(String field : configMap.keySet()){
            if(field.startsWith(prefix)){
                map.put(field, configMap.get(field));
            }
        }
        return map;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ConfigDetail{");
        sb.append("configMap=").append(configMap);
        sb.append('}');
        return sb.toString();
    }
}
