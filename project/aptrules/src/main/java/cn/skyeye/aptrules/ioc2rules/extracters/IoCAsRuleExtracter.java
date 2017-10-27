package cn.skyeye.aptrules.ioc2rules.extracters;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ARContext;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Description:
 *    ioc解析器
 * @author LiXiaoCong
 * @version 2017/10/13 10:53
 */
public abstract class IoCAsRuleExtracter {
    protected final Logger logger = Logger.getLogger(IoCAsRuleDBExtracter.class);

    protected ARConf arConf;

    private List<VagueRule> rules;

    //ioc总量
    private int iocCount;
    //有效的ioc总数
    private int effectIocCount;

    public IoCAsRuleExtracter() {
        this.arConf = ARContext.get().getArConf();
        this.rules = Lists.newArrayList();
    }

    protected void addIoCCount(){
        this.iocCount += 1;
    }

    protected void addEffectIoCCount(){
        this.effectIocCount += 1;
    }

    protected void addRule(VagueRule rule){
        this.rules.add(rule);
    }

    public abstract void extract(Map<String, Object> ioc);

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
