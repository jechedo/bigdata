package cn.skyeye.aptrules;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/25 15:29
 */
public class Record {

    protected Map<String, Object> data;

    public Record(Map<String, Object> data) {
        if(data == null){
            this.data = Maps.newHashMap();
        }else{
            this.data = data;
        }
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void put(String key, Object value){
        if(key != null && value != null){
            this.data.put(key, value);
        }
    }

    public String getString(String key){
        return  getString(key, null);
    }

    public String getString(String key, String defaultValue){
        Object o = data.get(key);
        return  o == null ? defaultValue : String.valueOf(o);
    }

    public String getStringOnce(String defaultValue, String ... keys){
        String res = defaultValue;
        Object o;
        for(String key : keys) {
            o = data.get(key);
            if (o != null) {
               res = String.valueOf(o);
               break;
            }
        }
        return res;
    }

    public int getInt(String key){
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue){
        Object o = data.get(key);
        int res = defaultValue;
        if(o != null){
            if(o instanceof Integer){
                res = (int)o;
            }else{
                try {
                    res =  Integer.parseInt(String.valueOf(o));
                } catch (NumberFormatException e) {}
            }
        }
        return res;
    }

    public int getIntOnce(int defaultValue, String ... keys){
        int res = defaultValue;
        Object o;
        for(String key : keys) {
            o = data.get(key);
            if (o != null) {
                if (o instanceof Integer) {
                    res = (int) o;
                    break;
                } else {
                    try {
                        res = Integer.parseInt(String.valueOf(o));
                        break;
                    } catch (NumberFormatException e) {}
                }
            }
        }
        return res;
    }

    public long getLong(String key){
        return getLong(key, 0);
    }

    public long getLong(String key, long defaultValue){
        Object o = data.get(key);
        long res = defaultValue;
        if(o != null){
            if(o instanceof Long){
                res = (long)o;
            }else{
                try {
                    res =  Long.parseLong(String.valueOf(o));
                } catch (NumberFormatException e) {}
            }
        }
        return res;
    }

    public long getLongOnce(long defaultValue, String ... keys){
        long res = defaultValue;
        Object o;
        for(String key : keys) {
            o = data.get(key);
            if (o != null) {
                if (o instanceof Long) {
                    res = (long) o;
                    break;
                } else {
                    try {
                        res = Long.parseLong(String.valueOf(o));
                        break;
                    } catch (NumberFormatException e) {}
                }
            }
        }
        return res;
    }

    public double getDouble(String key){
        return getDouble(key, 0.0);
    }

    public double getDouble(String key, double defaultValue){
        Object o = data.get(key);
        double res = defaultValue;
        if(o != null){
            if(o instanceof Double){
                res = (Double)o;
            }else{
                try {
                    res =  Double.parseDouble(String.valueOf(o));
                } catch (NumberFormatException e) {}
            }
        }
        return res;
    }

    public double getDoubleOnce(double defaultValue, String ... keys){
        double res = defaultValue;
        Object o;
        for(String key : keys) {
            o = data.get(key);
            if (o != null) {
                if (o instanceof Double) {
                    res = (double) o;
                    break;
                } else {
                    try {
                        res = Double.parseDouble(String.valueOf(o));
                        break;
                    } catch (NumberFormatException e) {}
                }
            }
        }
        return res;
    }

    public float getFloat(String key){
        return getFloat(key, 0.0f);
    }

    public float getFloat(String key, float defaultValue){
        Object o = data.get(key);
        float res = defaultValue;
        if(o != null){
            if(o instanceof Float){
                res = (Float) o;
            }else{
                try {
                    res =  Float.parseFloat(String.valueOf(o));
                } catch (NumberFormatException e) {}
            }
        }
        return res;
    }

    public float getFloatOnce(float defaultValue, String ... keys){
        float res = defaultValue;
        Object o;
        for(String key : keys) {
            o = data.get(key);
            if (o != null) {
                if (o instanceof Float) {
                    res = (float) o;
                    break;
                } else {
                    try {
                        res = Float.parseFloat(String.valueOf(o));
                        break;
                    } catch (NumberFormatException e) {}
                }
            }
        }
        return res;
    }

    public boolean getBoolean(String key){
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue){
        Object o = data.get(key);
        boolean res = defaultValue;
        if(o != null){
            if(o instanceof Boolean){
                res = (Boolean) o;
            }else{
                try {
                    res =  Boolean.parseBoolean(String.valueOf(o));
                } catch (NumberFormatException e) {}
            }
        }
        return res;
    }

    public boolean getBooleanOnce(boolean defaultValue, String ... keys){
        boolean res = defaultValue;
        Object o;
        for(String key : keys) {
            o = data.get(key);
            if (o != null) {
                if (o instanceof Boolean) {
                    res = (boolean) o;
                    break;
                } else {
                    try {
                        res = Boolean.parseBoolean(String.valueOf(o));
                        break;
                    } catch (NumberFormatException e) {}
                }
            }
        }
        return res;
    }

    public String[] getStringArray(String key){
        return  getStringArray(key, null);
    }

    public String[] getStringArray(String key, String[] defaultValue){
        return getStringArray(key, defaultValue, ",");
    }

    public String[] getStringArray(String key, String[] defaultValue, String spliter){
        Object o = data.get(key);
        String[] res = defaultValue;
        if(o != null) {
            if (o instanceof String[]) {
                res = (String[]) o;
            } else {
                res = String.valueOf(o).split(spliter);
            }
        }
        return res;
    }


    public List<String> getStringList(String key){
        return  getStringList(key, null);
    }

    public List<String> getStringList(String key, List<String> defaultValue){
        return getStringList(key, defaultValue, ",");
    }

    public List<String> getStringList(String key,  List<String> defaultValue, String spliter){
        Object o = data.get(key);
        List<String> res = defaultValue;
        if(o != null) {
            if (o instanceof List) {
                res = (List<String>) o;
            } else {
                res = Lists.newArrayList(String.valueOf(o).split(spliter));
            }
        }
        return res;
    }

    public Set<String> getStringSet(String key){
        return  getStringSet(key, null);
    }

    public Set<String> getStringSet(String key, Set<String> defaultValue){
        return getStringSet(key, defaultValue, ",");
    }

    public Set<String> getStringSet(String key,  Set<String> defaultValue, String spliter){
        Object o = data.get(key);
        Set<String> res = defaultValue;
        if(o != null) {
            if (o instanceof Set) {
                res = (Set<String>) o;
            } else {
                res = Sets.newHashSet(String.valueOf(o).split(spliter));
            }
        }
        return res;
    }

    public JSONArray getJsonArray(String key){
        return getJsonArray(key, null);
    }

    public JSONArray getJsonArray(String key, JSONArray defaultValue){
        return getJsonArray(key, defaultValue, ",");
    }

    public JSONArray getJsonArray(String key,  JSONArray defaultValue, String spliter){
        Object o = data.get(key);
        JSONArray res = defaultValue;
        if(o != null) {
            if (o instanceof JSONArray) {
                res = (JSONArray) o;
            } else {
                res = new JSONArray(Lists.newArrayList(String.valueOf(o).split(spliter)));
            }
        }
        return res;
    }


    public <K, V> Map<K, V> getMap(String key,  Map<K, V> defaultValue){
        Object o = data.get(key);
        Map<K, V> res = defaultValue;
        if(o != null) {
            if (o instanceof Map) {
                res = (Map<K, V>) o;
            }
        }
        return res;
    }



}
