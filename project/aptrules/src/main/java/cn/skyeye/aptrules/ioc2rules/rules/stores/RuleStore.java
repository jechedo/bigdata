package cn.skyeye.aptrules.ioc2rules.rules.stores;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ioc2rules.rules.Ruler;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/27 9:50
 */
public abstract class RuleStore {
    protected final Logger logger = Logger.getLogger(RuleStore.class);

    protected ARConf arConf;

    public RuleStore(ARConf arConf){
        this.arConf = arConf;
    }

    public abstract List<VagueRule> allRules(Ruler.RuleID ruleID) throws Exception;

    public abstract void overrideRules(Ruler.RuleID ruleID, List<VagueRule> vagueRules) throws Exception;
}
