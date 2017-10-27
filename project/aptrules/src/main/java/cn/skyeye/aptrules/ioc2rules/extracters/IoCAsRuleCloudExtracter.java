package cn.skyeye.aptrules.ioc2rules.extracters;

import cn.skyeye.aptrules.ARContext;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description:
 *     将规则转成rules，需要自己定义存储结构
 *
 * @author LiXiaoCong
 * @version 2017/10/13 10:58
 */
public class IoCAsRuleCloudExtracter extends IoCAsRuleExtracter {

    private String ruleType;
    private int tid;
    private AtomicLong ruleId;

    public IoCAsRuleCloudExtracter(){
        this("自定义情报告警", 0,
                ARContext.get().getArConf().getCustomRuleIdStart());
    }

    public IoCAsRuleCloudExtracter(String ruleType, int tid, long ruleIdBase){
        this.ruleType = ruleType;
        this.tid = tid;
        this.ruleId = new AtomicLong(ruleIdBase);
    }

    @Override
    public void extract(Map<String, Object> ioc) {


    }


}
