package cn.skyeye.aptrules.ioc2rules.rules.stores;

import cn.skyeye.aptrules.ARConf;
import org.apache.log4j.Logger;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/27 9:50
 */
public class RuleStore {
    protected final Logger logger = Logger.getLogger(RuleStore.class);

    protected ARConf arConf;

    public RuleStore(ARConf arConf){
        this.arConf = arConf;
    }
}
