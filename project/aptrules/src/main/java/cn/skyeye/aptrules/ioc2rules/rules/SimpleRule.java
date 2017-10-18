package cn.skyeye.aptrules.ioc2rules.rules;

import cn.skyeye.aptrules.ARConf;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Description:
 *    简单的规则，只需要验证规则条件与日志数据是否相等即可
 * @author LiXiaoCong
 * @version 2017/10/18 10:40
 */
public class SimpleRule extends Rule{

    protected ARConf arConf;

    private Map<String, Object> simpleRuleInfos = Maps.newHashMap();

    private HashMultimap<String, String> simples = HashMultimap.create();

    private Set<String> extraFields = Sets.newHashSet();

    public SimpleRule(ARConf arConf) {
        super();
        this.arConf = arConf;
    }

    public SimpleRule(SimpleRule rule) {
        super(rule);
        this.arConf = rule.arConf;
    }

    public void addSimpleRuleInfo(String field, Object value){
        if(field != null && value != null) {
            this.simpleRuleInfos.put(field, value);

            String valueStr = String.valueOf(value);
            addSimpleRules(field, valueStr, false);
        }
    }

    protected void addSimpleRules(String field, String valueStr, boolean extra) {
        if(valueStr != null) {
            if (arConf.isIocTypeMoreValueFields(field) && valueStr.contains(",")) {
                Iterable<String> split = Splitter.on(",").omitEmptyStrings().split(valueStr);
                simples.putAll(field, split);
            } else {
                simples.put(field, valueStr);
            }

            if(extra) this.extraFields.add(field);
        }
    }


    /**
     *  返回已匹配的字段
     * @param record
     * @return
     */
    public Set<String> matches(Map<String, Object> record){

        Set<String> keySet = Sets.newHashSet(record.keySet());
        Iterator<String> iterator = keySet.iterator();
        String field;
        Object value;
        while (iterator.hasNext()){
            field = iterator.next();
            //读取
            value = record.get(field);

            if(value == null
                    || !simples.containsEntry(field, String.valueOf(value))){
                if(extraFields.contains(field)){
                    iterator.remove();
                }else{
                    return null;
                }
            }
        }

        return keySet;
    }

    public static void main(String[] args) {
        String str = "*556*12453*12554*";
        int i = str.indexOf("12453");

        String substring = str.substring(i + "12453".length());
        System.out.println(substring);

    }

}
