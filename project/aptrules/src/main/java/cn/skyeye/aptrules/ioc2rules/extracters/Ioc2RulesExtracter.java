package cn.skyeye.aptrules.ioc2rules.extracters;

import cn.skyeye.aptrules.ARContext;
import cn.skyeye.aptrules.ARUtils;
import cn.skyeye.aptrules.ioc2rules.Rule;
import cn.skyeye.common.hash.Md5;
import cn.skyeye.common.json.Jsons;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description:
 *     将规则转成rules，需要自己定义存储结构
 *     由于是实时告警，为了提升告警效率，采用告警字段单独成rule的方式：
 *          1. 根据ioc中的type字段获取此ioc中告警匹配字段warn_fields。
 *          2. 遍历warn_fields获取到warn_field，并以此字段与其值合成rule的key，并将其他告警字段信息存入此rule中
 *     简单来说，就是规则是以告警匹配字段为主，当日志数据过来的时候，依次提取日志数据中的告警匹配字段查询rule,再验证
 *     rule中其他的告警匹配信息。
 * @author LiXiaoCong
 * @version 2017/10/13 10:58
 */
public class Ioc2RulesExtracter extends Extracter {

    private final Logger logger = Logger.getLogger(Ioc2RulesExtracter.class);

    private HashMultimap<String, Rule> rules;
    private String ruleType;
    private int tid;
    private AtomicLong ruleId;

    //ioc总量
    private int iocCount;
    //有效的ioc总数
    private int effectIocCount;

    public Ioc2RulesExtracter(){
        this("自定义情报告警", 0,
                ARContext.get().getArConf().getCustomRuleIdStart());
    }

    public Ioc2RulesExtracter(String ruleType, int tid, long ruleIdBase){
        this.rules = HashMultimap.create();
        this.iocCount = 0;
        this.effectIocCount = 0;
        this.ruleType = ruleType;
        this.tid = tid;
        this.ruleId = new AtomicLong(ruleIdBase);
    }

    @Override
    public void extract(Map<String, Object> ioc) {

        iocCount++;

        //判断是否为有效的ioc
        boolean active = (boolean) ioc.get("active");
        boolean export = (boolean) ioc.get("export");
        int confidence = (int) ioc.get("confidence");
        Object typeObj = ioc.get("type");
        if(typeObj != null && active && export && confidence == 80){

            effectIocCount++;
            String type = String.valueOf(typeObj).toLowerCase();

            //根据type获取desc_key
            String descKey = getDescKey(type);
            createRules(ioc, type, descKey);

            ioc.put("effect", 1);
        }
    }

    /**
     * 根据type生成规则
     *      一个ioc生成一条规则
     *      type中有匹配字段的逻辑表述， 再都为and的前提下 只取第一个匹配字段作为主匹配字段。
     * @param ioc
     * @param type
     * @param descKey
     */
    private void createRules(Map<String, Object> ioc, String type, String descKey){

        Object ipDomainObj = ioc.get("ip_or_domain");
        String ipDomain = "";
        if(ipDomainObj != null)ipDomain = String.valueOf(ipDomainObj).toLowerCase();

        Rule ruleModel = createRuleModel(ioc, type, descKey);
        switch (type){
            case "host":
                String  ruleHost = ARUtils.concat("host_md5", Md5.Md5_32(ipDomain));
                addNewRule(ruleModel, ruleHost);
                break;
            case "dip":
                String ruleIp = ARUtils.concat("dip", ipDomain);
                addNewRule(ruleModel, ruleIp);
                break;
            case "dip:dport":
                Object port = ioc.get("port");
                ruleIp = ARUtils.concat("dip", ipDomain);
                String rulePort = ARUtils.concat("dport", port);
                addNewRule(ruleModel, ruleIp, rulePort);
                break;
            case "host:dport":
                port = ioc.get("port");
                ruleHost = ARUtils.concat("host_md5", Md5.Md5_32(ipDomain));
                rulePort = ARUtils.concat("dport", port);

                addNewRule(ruleModel, ruleHost, rulePort);
                break;
            case "host:dport:uri":
                port = ioc.get("port");
                ruleHost = ARUtils.concat("host_md5", Md5.Md5_32(ipDomain));
                rulePort = ARUtils.concat("dport", port);

                String[] uris = listUris(ioc);
                String[] roleKeys = new String[uris.length + 1];
                roleKeys[1] = rulePort;
                for(int i = 0; i < uris.length; i++){
                    roleKeys[1 + i] = ARUtils.concat("uri", uris[i]);
                }
                addNewRule(ruleModel, ruleHost, ARUtils.concatWithSeparator(",", roleKeys));
                break;
            case "dip:dport:uri":
                port = ioc.get("port");
                ruleIp = ARUtils.concat("dip", ipDomain);
                rulePort = ARUtils.concat("dport", port);

                uris = listUris(ioc);
                roleKeys = new String[uris.length + 1];
                roleKeys[1] = rulePort;
                for(int i = 0; i < uris.length; i++){
                    roleKeys[1 + i] = ARUtils.concat("uri", uris[i]);
                }
                addNewRule(ruleModel, ruleIp, ARUtils.concatWithSeparator(",", roleKeys));
                break;
            case "host:uri":
                ruleHost = ARUtils.concat("host_md5", Md5.Md5_32(ipDomain));
                uris = listUris(ioc);
                roleKeys = new String[uris.length];
                for(int i = 0; i < uris.length; i++){
                    roleKeys[i] = ARUtils.concat("uri", uris[i]);
                }
                addNewRule(ruleModel, ruleHost, ARUtils.concatWithSeparator(",", roleKeys));
                break;
            case "md5":
                Object md5 = ioc.get("md5");
                if(md5 != null){
                    String ruleMd5 = ARUtils.concat("md5", md5);
                    addNewRule(ruleModel, ruleMd5);
                }
                break;
            default:
                logger.error(String.format("unexpected info_type:%s", type));
                break;
        }
    }


    /**
     * type = host:dport:uri
     * ruleKeys = [host_md5:xx, dport:md5:xx, uri:xx]
     *
     * @param ruleKeys
     */
    private void createRules(Rule ruleModel, String ... ruleKeys){

        String rule;
        if(ruleKeys.length > 1) {
            String otherRule;
            String baseOtherRule = ARUtils.concat(",", ruleKeys);
            for (int i = 0; i < ruleKeys.length; i++) {
                rule = ruleKeys[i];
                if (i == 0) {
                    otherRule = baseOtherRule.substring(rule.length() + 1);
                } else {
                    otherRule = baseOtherRule.replace(String.format(",%s", rule), "");
                }
                addNewRule(ruleModel, rule, otherRule);
            }
        }else if(ruleKeys.length == 1){
            rule = ruleKeys[0];
            addNewRule(ruleModel, rule);
        }
    }

    private void addNewRule(Rule ruleModel, String ruleKey){
       addNewRule(ruleModel, ruleKey, null);
    }

    private void addNewRule(Rule ruleModel, String ruleKey, String otherRule){
        Rule rule = ruleModel; //new Rule(ruleModel); 一条ioc只生成一条rule，因此不需要克隆
        rule.setRule_id(ruleId.getAndIncrement());
        rule.setRule(ruleKey);
        rule.setOther_rule(otherRule);
        rules.put(ruleKey, rule);
    }

    private Rule createRuleModel(Map<String, Object> ioc, String type, String descKey){

        Rule model = new Rule();
        model.setIoc_type(type);
        model.setEffect_ioc_count(1);
        model.setState("green");
        model.setDesc_key(descKey);

        String descJson = createDescJson(ioc, type);
        model.setDesc_json(descJson);

        model.setIds(Jsons.obj2JsonString(Lists.newArrayList(ioc.get("id"))));

        return model;
    }

    private String createDescJson(Map<String, Object> ioc, String type){

        Map<String, Object> descMap = Maps.newHashMap();
        descMap.put("tid", tid);
        descMap.put("desc", ioc.get("desc"));
        descMap.put("nid", String.valueOf(ioc.get("nid")));
        descMap.put("type",ruleType);

        Map<String, Object> desc = Maps.newHashMap();
        String descJsonKey = putDescJsonKey(ioc, type);
        desc.put(descJsonKey, descMap);

        return Jsons.obj2JsonString(desc);
    }

    private String getDescKey(String type){

        String[] parts = type.split(":");
        String part;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < parts.length; i++){
            part = parts[i];
            if("host".equals(part)){
                parts[i] = "host_md5";
            }
            sb.append(":").append(part);
        }

        return sb.substring(1);
    }

    private String putDescJsonKey(Map<String, Object> ioc, String type){

        Object ipDomainObj = ioc.get("ip_or_domain");
        String ipDomain = "";
        if(ipDomainObj != null)ipDomain = String.valueOf(ipDomainObj).toLowerCase();

        if(type.contains("host")){
            ipDomain = Md5.Md5_32(ipDomain); //md5加密
        }

        String descJsonKey = "";
        switch (type) {
            case "dip:dport:uri":
            case "host:dport:uri":
                descJsonKey = String.format("%s:%s", ipDomain, ioc.get("port"));
                break;
            case "host:uri":
                descJsonKey = ipDomain;
                break;
            case "dip:dport":
            case "host:dport":
                descJsonKey = String.format("%s:%s", ipDomain, ioc.get("port"));
                break;
            case "dip":
            case "host":
                descJsonKey = ipDomain;
                break;
            case "md5":
                Object md5 = ioc.get("md5");
                if (md5 != null) descJsonKey = String.valueOf(md5);
                break;
            default:
                logger.error(String.format("unexpected info_type:%s", type));
        }
        ioc.put("desc_json_key", descJsonKey);
        return descJsonKey;
    }

    private String putIocKey(Map<String, Object> ioc, String type){

        Object ipDomainObj = ioc.get("ip_or_domain");
        String ipDomain = "";
        if(ipDomainObj != null)ipDomain = String.valueOf(ipDomainObj).toLowerCase();

        String iocKey = "";
        switch (type){
            case "dip:dport:uri":
            case "host:dport:uri":
                iocKey = String.format("%s:%s:%s", ipDomain, ioc.get("port"), ioc.get("url"));
                break;
            case "host:uri":
                iocKey = String.format("%s:%s", ipDomain, ioc.get("url"));
                break;
            case "dip:dport":
            case "host:dport":
                iocKey = String.format("%s:%s", ipDomain, ioc.get("port"));
                break;
            case "dip":
            case "host":
                iocKey = ipDomain;
                break;
            case "md5":
                Object md5 = ioc.get("md5");
                if(md5 != null) iocKey = String.valueOf(md5);
                break;
            default:
                logger.error(String.format("unexpected info_type:%s", type));
        }

        ioc.put("ioc_key", iocKey);
        return iocKey;
    }

    private String[] listUris(Map<String, Object> ioc) {
        Object urisObj = ioc.get("url");
        if (urisObj != null) {
            String uris = String.valueOf(urisObj);
            return uris.split(",");
        }
        return new String[]{};
    }
    public HashMultimap<String, Rule> getRules() {
        return rules;
    }

    public int getIocCount() {
        return iocCount;
    }

    public int getEffectIocCount() {
        return effectIocCount;
    }
}
