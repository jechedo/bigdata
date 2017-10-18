package cn.skyeye.aptrules.ioc2rules.rules;

/**
 * Description:
 *   告警规则
 * @author LiXiaoCong
 * @version 2017/10/13 12:04
 */
public class Rule {

    protected int id;
    protected long rule_id;
    protected String rule;
    protected String other_rule;
    protected String desc_key;
    protected String desc_json;
    protected String state;
    protected String ioc_type;
    protected String ids;

    protected int version_id = 0;
    protected int source = 0;
    protected long stime = 0L;
    protected long etime = 0L;
    protected long check_time = 0;
    protected long pubtime = System.currentTimeMillis();
    protected int effect_ioc_count = 1;

    public Rule() {}

    public Rule(Rule rule){
        setId(rule.id);
        setRule_id(rule.rule_id);
        setRule(rule.rule);
        setOther_rule(rule.other_rule);
        setDesc_key(rule.desc_key);
        setDesc_json(rule.desc_json);
        setState(rule.state);
        setIoc_type(rule.ioc_type);
        setIds(rule.ids);
        setVersion_id(rule.version_id);
        setSource(rule.source);
        setStime(rule.stime);
        setCheck_time(rule.check_time);
        setPubtime(rule.pubtime);
        setEffect_ioc_count(rule.effect_ioc_count);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion_id() {
        return version_id;
    }

    public void setVersion_id(int version_id) {
        this.version_id = version_id;
    }

    public long getRule_id() {
        return rule_id;
    }

    public void setRule_id(long rule_id) {
        this.rule_id = rule_id;
    }

    public String getDesc_key() {
        return desc_key;
    }

    public void setDesc_key(String desc_key) {
        this.desc_key = desc_key;
    }

    public String getDesc_json() {
        return desc_json;
    }

    public void setDesc_json(String desc_json) {
        this.desc_json = desc_json;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getIoc_type() {
        return ioc_type;
    }

    public void setIoc_type(String ioc_type) {
        this.ioc_type = ioc_type;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public long getStime() {
        return stime;
    }

    public void setStime(long stime) {
        this.stime = stime;
    }

    public long getEtime() {
        return etime;
    }

    public void setEtime(long etime) {
        this.etime = etime;
    }

    public long getCheck_time() {
        return check_time;
    }

    public void setCheck_time(long check_time) {
        this.check_time = check_time;
    }

    public long getPubtime() {
        return pubtime;
    }

    public void setPubtime(long pubtime) {
        this.pubtime = pubtime;
    }

    public int getEffect_ioc_count() {
        return effect_ioc_count;
    }

    public void setEffect_ioc_count(int effect_ioc_count) {
        this.effect_ioc_count = effect_ioc_count;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getOther_rule() {
        return other_rule;
    }

    public void setOther_rule(String other_rule) {
        this.other_rule = other_rule;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Rule{");
        sb.append("id=").append(id);
        sb.append(", rule_id=").append(rule_id);
        sb.append(", rule='").append(rule).append('\'');
        sb.append(", other_rule='").append(other_rule).append('\'');
        sb.append(", desc_key='").append(desc_key).append('\'');
        sb.append(", desc_json='").append(desc_json).append('\'');
        sb.append(", state='").append(state).append('\'');
        sb.append(", ioc_type='").append(ioc_type).append('\'');
        sb.append(", ids='").append(ids).append('\'');
        sb.append(", version_id=").append(version_id);
        sb.append(", source=").append(source);
        sb.append(", stime=").append(stime);
        sb.append(", etime=").append(etime);
        sb.append(", check_time=").append(check_time);
        sb.append(", pubtime=").append(pubtime);
        sb.append(", effect_ioc_count=").append(effect_ioc_count);
        sb.append('}');
        return sb.toString();
    }
}
