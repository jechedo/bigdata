package cn.skyeye.aptrules.ioc2rules.iocs.stores;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ioc2rules.extracters.IoCAsRuleExtracter;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/27 9:48
 */
public class IoCCloudStore extends IoCStore {
    public IoCCloudStore(ARConf arConf) {
        super(arConf);
    }

    @Override
    public void extractIoCAsRules(IoCAsRuleExtracter ioCAsRuleExtracter) throws Exception{

    }

    @Override
    public void updateStatus(boolean syncSuccess) {

    }
}
