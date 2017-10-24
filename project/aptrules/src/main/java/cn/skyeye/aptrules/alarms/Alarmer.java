package cn.skyeye.aptrules.alarms;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ARUtils;
import cn.skyeye.aptrules.ioc2rules.Ruler;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *   根据告警规则 以及数据 生成 告警表单
 * @author LiXiaoCong
 * @version 2017/10/20 15:17
 */
public class Alarmer {

    private ARConf arConf;

    public Alarmer(ARConf arConf){
        this.arConf = arConf;
    }

    public List<Alarm> createAlarm(Map<String, Object> record, Ruler.Hits hits){
        int size = hits.getHitSize();
        List<Alarm> alarms = Lists.newArrayListWithCapacity(size);
        for (Map.Entry<Ruler.IndexKey, VagueRule> entry : hits.getHitSet()) {
            alarms.add(createAlarm(record, entry.getKey(), entry.getValue()));
        }
        return alarms;
    }


    /**
     *
     * 需要根据实际情况 更换字段 以及判断是否为重复警告
     *
     * @param record
     * @param rule
     * @return
     */
    public Alarm createAlarm(Map<String, Object> record, Ruler.IndexKey ruleKey, VagueRule rule){
        Alarm alarm = new Alarm(ruleKey.getRuleDataKey(), record); // Alarm的id还有待验证

        Map<String, Object> iocDetailMap = createIocDetailMap(record, ruleKey, rule);


        return alarm;
    }

    /**
     * 根据命中信息 生成告警使用 iocDetail
     * @param record
     * @param ruleKey
     * @param rule
     * @return
     */
    private Map<String, Object> createIocDetailMap(Map<String, Object> record, Ruler.IndexKey ruleKey, VagueRule rule) {
        Map<String, Object> descJsonMap = rule.getDescJsonInMap();
        descJsonMap.put("rule_state", rule.getState());
        descJsonMap.put("rule_id", rule.getRule_id());

        List<String> ruleFields = ruleKey.getRuleFields();

        if(ruleFields.size() == 1 && "md5".equals(ruleFields.get(0))){
            String dataField = ruleKey.getDataFieldByRuleField("md5");
            Object value = ruleKey.getDataByRuleField("md5");
            switch (dataField){
                case "file_md5":
                    descJsonMap.put("desc_key", "file_md5");
                    descJsonMap.put("ioc", value);
                    descJsonMap.put("md5",value);
                    descJsonMap.put("name", ARUtils.getValueByKeyInMap(record, "file_name", ""));
                    break;
                case "process_md5":
                    descJsonMap.put("desc_key", "process_md5");
                    descJsonMap.put("ioc", value);
                    break;
                default:
                    //附件的操作相关
                    break;
            }
        }else {
            List<Object> ruleDatas = ruleKey.getRuleDatas();
            String ioc = Joiner.on(":").join(ruleDatas).toLowerCase();
            if(ruleFields.contains("dport")){
                Object dport = ruleKey.getDataByRuleField("dport");
                ioc = ioc.replace(String.format(":%s:%s", dport, dport), String.format(":%s", dport));
            }

            if(ruleFields.contains("uri")){
                String uri = String.valueOf(ruleKey.getDataByRuleField("uri")).toLowerCase();
                if(uri.startsWith("/")){
                    ioc = ioc.replace(String.format(":%s", uri), uri);
                }else {
                    ioc = ioc.replace(String.format(":%s", uri), String.format("/%s", uri));
                }
            }
            descJsonMap.put("desc_key", rule.getDesc_key());
            descJsonMap.put("ioc", ioc);
        }

        return descJsonMap;
    }

    /**
     * 判断是否为重复告警
     * @param iocDetailMap
     * @return
     */
    private boolean isRepeatAlarm(Map<String, Object> iocDetailMap){

        return false;
    }

    public static void main(String[] args) {
        //2017-10-24T14:11:58.942+0800
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        System.out.println(simpleDateFormat.format(new Date()));
    }
}
