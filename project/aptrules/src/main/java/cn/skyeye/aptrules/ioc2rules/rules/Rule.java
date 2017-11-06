package cn.skyeye.aptrules.ioc2rules.rules;

import cn.skyeye.common.json.Jsons;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Description:
 *   告警规则
 * @author LiXiaoCong
 * @version 2017/10/13 12:04
 */
public class Rule {

    protected static final Logger logger = Logger.getLogger(Rule.class);

    protected Map<String, Object> record;

    public Rule() {
        record = Maps.newConcurrentMap();
        record.put("rule_id", -1L);
        record.put("rule", "");
        //record.put("other_rule", null);
        record.put("desc_key", "");
        record.put("desc_json", "");
        record.put("state", "");
        record.put("ioc_type", "");
        record.put("ids", "");
        record.put("version_id", 0);
        record.put("source", 0);
        record.put("stime", 0L);
        record.put("etime", 0L);
        record.put("check_time", 0L);
        record.put("pubtime", System.currentTimeMillis());
        record.put("effect_ioc_count", 0);
    }

    public Rule(Rule rule){
        record = Maps.newConcurrentMap();
        record.putAll(rule.record);
    }

    public int getVersion_id() {
        return getValue("version_id", 0);
    }

    public void setVersion_id(int version_id) {
        this.record.put("version_id", version_id);
    }

    public long getRule_id() {
        return getValue("rule_id", -1L);
    }

    public void setRule_id(long rule_id) {
        this.record.put("rule_id", rule_id);
    }

    public String getDesc_key() {
        return getValue("desc_key", null);
    }

    public void setDesc_key(String desc_key) {
        this.record.put("desc_key", desc_key);
    }

    public String getDesc_json() {
        return getValue("desc_json", null);
    }

    public Map<String, Object> getDescJsonInMap() {
        String desc_json = getValue("desc_json", null);
        if(desc_json != null){
            try {
                return Jsons.toMap(desc_json);
            } catch (Exception e) {
               logger.error(null, e);
            }
        }
        return Maps.newHashMap();
    }

    public void setDesc_json(String desc_json) {
        this.record.put("desc_json", desc_json);
    }

    public String getState() {
        return getValue("state", null);
    }

    public void setState(String state) {
        this.record.put("state", state);
    }

    public String getIoc_type() {
        return getValue("ioc_type", null);
    }

    public void setIoc_type(String ioc_type) {
        this.record.put("ioc_type", ioc_type);
    }

    public String getIds() {
        return getValue("ids", null);
    }

    public void setIds(String ids) {
        this.record.put("ids", ids);
    }

    public int getSource() {
        return getValue("source", 0);
    }

    public void setSource(int source) {
        this.record.put("source", source);
    }

    public long getStime() {
        return getValue("stime", 0L);
    }

    public void setStime(long stime) {
        this.record.put("stime", stime);
    }

    public long getEtime() {
        return getValue("etime", 0L);
    }

    public void setEtime(long etime) {
        this.record.put("etime", etime);
    }

    public long getCheck_time() {
        return getValue("check_time", 0L);
    }

    public void setCheck_time(long check_time) {
        this.record.put("check_time", check_time);
    }

    public long getPubtime() {
        return getValue("pubtime", System.currentTimeMillis());
    }

    public void setPubtime(long pubtime) {
        this.record.put("pubtime", pubtime);
    }

    public int getEffect_ioc_count() {
        return getValue("effect_ioc_count", 0);
    }

    public void setEffect_ioc_count(int effect_ioc_count) {
        this.record.put("effect_ioc_count", effect_ioc_count);
    }

    public String getRule() {
        return getValue("rule", null);
    }

    public void setRule(String rule) {
        this.record.put("rule", rule);
    }

    public String getOther_rule() {
        return getValue("other_rule", null);
    }

    public void setOther_rule(String other_rule) {
        this.record.put("other_rule", other_rule);
    }

    public void setKV(String key, Object value){
        this.record.put(key, value == null ? "" : value);
    }

    private <T> T getValue(String key, T defualt){
        Object o = this.record.get(key);
        return o == null ? defualt : (T)o;
    }

    public Map<String, Object> getRuleMap() {
        return Maps.newHashMap(record);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Rule{");
        sb.append(record).append('}');
        return sb.toString();
    }
}
