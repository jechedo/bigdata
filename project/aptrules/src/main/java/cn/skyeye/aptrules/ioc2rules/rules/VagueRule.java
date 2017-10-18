package cn.skyeye.aptrules.ioc2rules.rules;

/**
 * Description:
 *   模糊匹配规则
 *     模糊匹配的模式分三类：前缀、后缀、中间至少一次模糊（类似 123.*.456.*.789.com）
 *     只有host和uri存在模糊匹配
 * @author LiXiaoCong
 * @version 2017/10/18 10:36
 */
public class VagueRule extends Rule{

    public VagueRule() {
        super();
    }

    public VagueRule(VagueRule rule) {
        super(rule);
    }
}
