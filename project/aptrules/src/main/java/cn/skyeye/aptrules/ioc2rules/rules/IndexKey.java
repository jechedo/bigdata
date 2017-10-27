package cn.skyeye.aptrules.ioc2rules.rules;

import com.google.common.collect.Lists;

import java.util.List;

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
}

