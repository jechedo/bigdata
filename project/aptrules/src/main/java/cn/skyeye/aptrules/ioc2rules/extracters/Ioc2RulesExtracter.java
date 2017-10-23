package cn.skyeye.aptrules.ioc2rules.extracters;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ARContext;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
import cn.skyeye.common.hash.Md5;
import cn.skyeye.common.json.Jsons;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description:
 *     将规则转成rules，需要自己定义存储结构
 *
 * @author LiXiaoCong
 * @version 2017/10/13 10:58
 */
public class Ioc2RulesExtracter extends Extracter {

    private final Logger logger = Logger.getLogger(Ioc2RulesExtracter.class);

    private ARConf arConf;
    private List<VagueRule> rules;
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
        this.arConf = ARContext.get().getArConf();
        this.rules = Lists.newArrayList();
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
            //ioc.put("effect", 1);
        }
    }

    /**
     *   根据配置 封装 VagueRule，并根据type分类
     *
     *
     * @param ioc
     * @param type
     * @param descKey
     */
    private void createRules(Map<String, Object> ioc, String type, String descKey){

        VagueRule ruleModel = createRuleModel(ioc, type, descKey);

        String[] typeFields = type.split(arConf.getIocTypeSeparator());
        String typeDataField;
        Object typeDataObj;
        String typeData;
        for(String typeField: typeFields){
            typeDataField = arConf.getIocTypeDataField(typeField);
            typeDataObj = ioc.get(typeDataField);
            if(typeDataObj != null){
                if(arConf.isIocVagueField(typeField)){
                    typeData = String.valueOf(typeDataObj);
                    if(typeData.contains("*")){
                        ruleModel.addVagueRuleInfo(typeField, typeDataObj);
                    }else {
                        ruleModel.addSimpleRuleInfo(typeField, typeDataObj);
                    }
                }else {
                    ruleModel.addSimpleRuleInfo(typeField, typeDataObj);
                }
            }else{
                logger.error(String.format("ioc中type字段%s对应的数据字段%s为空, ioc为：\n %s", typeField, typeDataField, ioc));
                return;
            }
        }

        rules.add(ruleModel);
    }

    private VagueRule createRuleModel(Map<String, Object> ioc, String type, String descKey){

        VagueRule model = new VagueRule(arConf);
        model.setRule_id(ruleId.getAndIncrement());
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
        //descMap.put("tid", tid);
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
                part = "host_md5";
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

    public List<VagueRule> getRules() {
        return rules;
    }

    public int getIocCount() {
        return iocCount;
    }

    public int getEffectIocCount() {
        return effectIocCount;
    }
}
