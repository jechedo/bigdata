package cn.skyeye.aptrules.ioc2rules.rules;

import cn.skyeye.common.json.Jsons;
import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 根据数据生成的查找规则的RuleKey
 *  由于Key生成的时候并不是严格按照规则中type里的字段，
 *  因此还存在一个对应的数据的DataKey
 *  DataKey和RuleKey的区别主要在于字段名称不同，内容相同，一个是对于数据而言的，一个是对于规则而言的。
 *  此对象主要是为了方便后期生成告警表单。
 */
public class IndexKey{
    private List<String> ruleFields;
    private List<Object> ruleDatas;
    private List<String> dataFields;
    private List<Object> datas;

    IndexKey(){
        this.ruleFields  = Lists.newArrayList();
        this.ruleDatas = Lists.newArrayList();
        this.dataFields  = Lists.newArrayList();
        this.datas     = Lists.newArrayList();
    }

    IndexKey(IndexKey indexKey){
        this.ruleFields  = Lists.newArrayList(indexKey.ruleFields);
        this.ruleDatas = Lists.newArrayList(indexKey.ruleDatas);
        this.dataFields  = Lists.newArrayList(indexKey.dataFields);
        this.datas     = Lists.newArrayList(indexKey.datas);
    }

    void addKV(String ruleField, Object ruleData, String dataField, Object data){
        this.ruleFields.add(ruleField);
        this.ruleDatas.add(ruleData);
        this.dataFields.add(dataField);
        this.datas.add(data);
    }

    public List<String> getRuleFields() {
        return ruleFields;
    }

    public String getDataFieldByRuleField(String ruleField){
        String dataField = null;
        int size = ruleFields.size();
        for (int i = 0; i < size; i++) {
            if(ruleField.equals(ruleFields.get(i))){
                dataField = dataFields.get(i);
                break;
            }
        }
        return dataField;
    }

    public Object getDataByRuleField(String ruleField){
        return getDataByFieldInDatas(ruleFields, datas, ruleField);
    }

    public String getRuleKey() {
        return getKeyString(ruleFields, ruleDatas);
    }

    public String getRuleDataKey() {
        return getKeyString(ruleFields, datas);
    }

    public List<String> getNoEmptyRuleFields(){
        List<String> res = Lists.newArrayList();
        int size = ruleDatas.size();
        for (int i = 0; i < size; i++) {
            if(ruleDatas.get(i) != null){
                res.add(ruleFields.get(i));
            }
        }
        return res;
    }

    public List<Object> getRuleDatas(){
        return datas;
    }

    private Object getDataByFieldInDatas(List<String> fields, List<Object> datas, String field){
        Object data = null;
        int size = fields.size();
        for (int i = 0; i < size; i++) {
            if(field.equals(fields.get(i))){
                data = datas.get(i);
                break;
            }
        }
        return data;
    }

    private String getKeyString(List<String> fields, List<Object> datas) {
        StringBuilder sb = new StringBuilder();
        int size = fields.size();
        Object value;
        for (int i = 0; i < size; i++) {
            sb.append(",").append(fields.get(i));
            value = datas.get(i);
            if(value != null){
                sb.append(":").append(value);
            }
        }
        return sb.substring(1);
    }

    public String getIndexKeyString(){
        Map<String, Object> map = new HashMap<>();
        map.put("ruleFields", ruleFields);
        map.put("ruleDatas", ruleDatas);
        map.put("dataFields", dataFields);
        map.put("datas", datas);
      return Jsons.obj2JsonString(map);
    }

    public static IndexKey newByIndexKeyString(String indexKeyString) throws Exception {
        Map<String, Object> res = Jsons.toMap(indexKeyString);
        IndexKey indexKey = new IndexKey();
        Set<Map.Entry<String, Object>> entries = res.entrySet();
        int n = 4;
        for(Map.Entry<String, Object> entry : entries){
            if(entry.getValue() instanceof JSONArray){
                JSONArray jsonArray = (JSONArray) entry.getValue();
                switch (entry.getKey()){
                    case "ruleFields":
                        indexKey.ruleFields = jsonArray.toJavaList(String.class);
                        n -= 1;
                        break;
                    case "ruleDatas":
                        indexKey.ruleDatas = jsonArray.toJavaList(Object.class);
                        n -= 1;
                        break;
                    case "dataFields":
                        indexKey.dataFields = jsonArray.toJavaList(String.class);
                        n -= 1;
                        break;
                    case "datas":
                        indexKey.datas = jsonArray.toJavaList(Object.class);
                        n -= 1;
                        break;
                }
            }
        }
        Preconditions.checkArgument(n < 1, String.format("不是标准的IndexKey: %s", indexKeyString));

        return indexKey;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IndexKey{");
        sb.append("ruleFields=").append(ruleFields);
        sb.append(", ruleDatas=").append(ruleDatas);
        sb.append(", dataFields=").append(dataFields);
        sb.append(", datas=").append(datas);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndexKey indexKey = (IndexKey) o;
        return getRuleDataKey().equals(indexKey.getRuleDataKey());
    }

    @Override
    public int hashCode() {
        int result = ruleFields.hashCode();
        result = 31 * result + datas.hashCode();
        return result;
    }

    public static void main(String[] args) throws Exception {

        Map<String, Object> map = new HashMap<>();
        map.put("ruleFields", Lists.newArrayList("name", "age", "address"));
        map.put("ruleDatas", Lists.newArrayList("jechedo", 21, null));

        Map<String, Object> rule = Maps.newHashMap();
        rule.put("simple", "hello");
        rule.put("vague", "world");
        map.put("rule", rule);

        String s = Jsons.obj2JsonString(map);
        System.out.println(s);

        Map<String, Object> res = Jsons.toMap(s);
        res.forEach((key, value) ->{
            System.err.println(key + " --> " + value + " --> " + value.getClass());
        });



    }

}

