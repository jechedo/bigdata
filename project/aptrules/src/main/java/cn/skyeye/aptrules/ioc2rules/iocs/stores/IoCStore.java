package cn.skyeye.aptrules.ioc2rules.iocs.stores;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ioc2rules.extracters.IoCAsRuleExtracter;
import org.apache.log4j.Logger;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/27 9:48
 */
public abstract class IoCStore {

    protected final Logger logger = Logger.getLogger(IoCStore.class);

    protected ARConf arConf;

    public IoCStore(ARConf arConf){
        this.arConf = arConf;
    }

    public abstract void extractIoCAsRules(IoCAsRuleExtracter ioCAsRuleExtracter) throws Exception;

    public abstract void updateStatus(boolean syncSuccess);

}
