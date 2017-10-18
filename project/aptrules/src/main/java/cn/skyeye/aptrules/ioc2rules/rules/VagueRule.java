package cn.skyeye.aptrules.ioc2rules.rules;

import cn.skyeye.aptrules.ARConf;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                split.forEach(val ->{
                        if(val.contains("*")){
                            //根据模糊匹配字符 * 切分
                            Iterable<String> vals = Splitter.on("*").omitEmptyStrings().split(val);
                            List<String> vagueLists = Lists.newArrayList(vals);
                            this.vagues.put(field, vagueLists);
                        }else {
                            addSimpleRules(field, val, true);
                        }
                });
            }
        }
    }

    public boolean matches(Map<String, Object> record) {
        Set<String> matches = simpleMatches(record);

        if(matches != null) {
            Set<String> fields = Sets.newHashSet(vagues.keySet());
            fields.removeAll(matches);
            return vagueMatches(record, fields);
        }

        return false;
    }

    private boolean vagueMatches(Map<String, Object> record,  Set<String> fields){

        Iterator<String> iterator = fields.iterator();
        String field;
        Object valueObj;
        String value;
        int matchTimes;

        Set<List<String>> vagueValss;

        while (iterator.hasNext()){
            field = iterator.next();
            valueObj = record.get(field);
            matchTimes = 0;

            if(valueObj == null) return false;

            vagueValss = vagues.get(field);
            for(List<String> vagueVals : vagueValss){
                value = String.valueOf(valueObj);
                int index = -1;
                for(String vagueVal : vagueVals){
                    index = value.indexOf(vagueVal);
                    if(index > -1){
                        value = value.substring(index + vagueVal.length());
                    }else{
                        break;
                    }
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
}
