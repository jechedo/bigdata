package cn.skyeye.aptrules.ioc2rules.rules;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.common.json.Jsons;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description:
 *   模糊匹配规则
 *     模糊匹配的模式分三类：前缀、后缀、中间至少一次模糊（类似 123.*.456.*.789.com）
 *     只有host和uri存在模糊匹配
 *   模糊匹配原则：
 *       由于是实时处理，即拿数据找规则，但是模糊的是规则而非数据，因此采用正则查找规则行不通
 *       这里事先对规则进行分类，根据数据获取对应分类的规则数据，再根据简单规则过滤，最后进行模糊规则过滤。
 *
 *       对于模糊规则，在规则生成的时候以 * 拆分成数组，然后依次判断数组中字符串是否再日志数据中顺序存在。
 *
 * @author LiXiaoCong
 * @version 2017/10/18 10:36
 */
public class VagueRule extends SimpleRule{

    private Map<String, Object> vagueRuleInfos = Maps.newHashMap();

    //模糊字段及其模糊的值
    protected HashMultimap<String, List<String>> vagues = HashMultimap.create();

    public VagueRule(ARConf arConf) {
        super(arConf);
    }

    public VagueRule(VagueRule rule) {
        super(rule);
    }

    public void addVagueRuleInfo(String field, Object value){
        if(field != null && value != null) {
            this.vagueRuleInfos.put(field, value);
            String vagueValue = String.valueOf(value);

            if (arConf.isIocTypeMoreValueFields(field) && vagueValue.contains(",")){
                Iterable<String> split = Splitter.on(",").omitEmptyStrings().split(vagueValue);
                split.forEach(val -> addVagues(field, val));
            }else{
                addVagues(field, vagueValue);
            }
        }
    }

    private void addVagues(String field, String vagueValue) {
        if(vagueValue.contains("*")){
            //根据模糊匹配字符 * 切分
            Iterable<String> vals = Splitter.on("*").omitEmptyStrings().split(vagueValue);
            List<String> vagueLists = Lists.newArrayList(vals);
            this.vagues.put(field, vagueLists);
        }else {
            addSimpleRules(field, vagueValue, true);
        }
    }

    public boolean matches(Ruler.IndexKey indexKey) {

        if(vagues.isEmpty()) return true;

        List<String> matches = indexKey.getNoEmptyRuleFields();
        Set<String> fields = Sets.newHashSet(vagues.keySet());
        fields.removeAll(matches);
        if(fields.isEmpty()) return true;

        return vagueMatches(indexKey, fields);
    }

    private boolean vagueMatches(Ruler.IndexKey indexKey,  Set<String> fields){

        Iterator<String> iterator = fields.iterator();
        String field;
        Object valueModel;
        String value;
        int matchTimes;

        Set<List<String>> vagueValss;

        while (iterator.hasNext()){
            field = iterator.next();
            valueModel = indexKey.getDataByRuleField(field);
            matchTimes = 0;

            if(valueModel == null) return false;
            value = String.valueOf(valueModel);

            vagueValss = vagues.get(field);
            for(List<String> vagueVals : vagueValss){
                /* 字段值是否是多个拼接的？ 暂时考虑的是单值 */
                int index = -1;
                for(String vagueVal : vagueVals){
                    index = value.indexOf(vagueVal, index + 1);
                    if(index < 0) break;
                }

                //判断此次是否匹配成功
                if(index > -1){
                    break;
                }else{
                    matchTimes += 1;
                }
            }

            //判断字段是否最终匹配成功
            if(matchTimes == vagueValss.size()){
                return false;
            }
        }

        return true;
    }


    /**
     * 规则在缓存中的索引
     *        实现的方式待改进
     */
    public Set<String> getRoleIndexKeys(){

        List<String> roleIndexFieldLevels = arConf.getRoleIndexFieldLevels();
        int size = roleIndexFieldLevels.size();

        String field;
        Object so;
        String sv;

        Object vo;
        List<StringBuilder> keys = Lists.newArrayList(new StringBuilder());
        List<StringBuilder> keyModels;
        StringBuilder model;
        String[] svs;
        for (int i = 0; i < size ; i++) {
            field = roleIndexFieldLevels.get(i);
            so = simpleRuleInfos.get(field);
            vo = vagueRuleInfos.get(field);

            if(so == null && vo == null) continue;
            if(so != null){
                sv = String.valueOf(so);
                if(arConf.isIocTypeMoreValueFields(field) && sv.contains(",")) {
                    svs = sv.split(",");
                }else {
                    svs = new String[]{sv};
                }

                keyModels = Lists.newArrayList(keys);
                keys = Lists.newArrayList();

                for(StringBuilder sb : keyModels){
                    for(String s : svs){
                        model = new StringBuilder(sb);
                        model.append(",").append(field).append(":").append(s);
                        keys.add(model);
                    }

                    if(vo != null){  //说明为 模糊匹配字段
                        model = new StringBuilder(sb);
                        model.append(",").append(field);
                        keys.add(model);
                    }
                }
            }

            if(vo != null && so == null){
                for(StringBuilder sb : keys){
                    sb.append(",").append(field);
                }
            }
        }

        Set<String> res = Sets.newHashSet();
        for (StringBuilder sb : keys){
            res.add(sb.substring(1));
        }

        return res;

    }

    /**
     * 参照 getRuleMap() 方法中封装json来解析
     * @param jsonRuleInfo
     */
    public void setJsonRuleInfo(String jsonRuleInfo){
        try {
            Map<String, Object> map = Jsons.toMap(jsonRuleInfo);
            Map<String, Object> simple = (Map<String, Object>) map.get("simple");
            Map<String, Object> vague = (Map<String, Object>) map.get("vague");

           simple.forEach((key, value) -> addSimpleRuleInfo(key, value));
           vague.forEach((key, value) -> addVagueRuleInfo(key, value));
        } catch (Exception e) {
            logger.error(null, e);
        }
    }

    @Override
    public Map<String, Object> getRuleMap() {
        Map<String, Object> record = super.getRuleMap();

        Map<String, Object> rule = Maps.newHashMap();
        rule.put("simple", simpleRuleInfos);
        rule.put("vague", vagueRuleInfos);

        record.put("rule", Jsons.obj2JsonString(rule));
        return record;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Rule{");
        sb.append(getRuleMap()).append('}');
        return sb.toString();
    }

    public static void main(String[] args) {
        long t = System.currentTimeMillis();
        Pattern p= Pattern.compile("www.aa.*as.s.*s");
        Matcher matcher = p.matcher("www.aaqiwiwas.sfkjewhfewifers");
        System.out.println(matcher.matches());
        System.out.println(System.currentTimeMillis() - t);

        Iterable<String> split = Splitter.on("*").omitEmptyStrings().split("www.aa*as.s*s");
        ArrayList<String> strings = Lists.newArrayList(split);

        String value = "www.aaqiwiwas.sfkjewhfewifers";
        t = System.currentTimeMillis();
        int index;
        boolean success = true;
        for(String vagueVal : strings){
            index = value.indexOf(vagueVal);
            if(index > -1){
                value = value.substring(index + vagueVal.length());
                System.out.println(value);
            }else{
                success = false;
                break;
            }
        }

        System.out.println(System.currentTimeMillis() -t);
        System.out.println(success);

    }
}
