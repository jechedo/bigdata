package cn.skyeye.aptrules.alarms;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

/**
 * Description:
 *   根据告警规则 以及数据 生成 告警表单
 * @author LiXiaoCong
 * @version 2017/10/20 15:17
 */
public class Alarmer {

    private ARConf arConf;

    public Alarmer(ARConf arConf){
        this.arConf = arConf;
    }

    public List<Alarm> createAlarm(Map<String, Object> record, List<VagueRule> rules){

        int size = rules.size();
        List<Alarm> alarms = Lists.newArrayListWithCapacity(size);
        for (int i = 0; i < size ; i++) {
            alarms.add(createAlarm(record, rules.get(i)));
        }
        return alarms;
    }

    public Alarm createAlarm(Map<String, Object> record, VagueRule rule){

        return null;
    }
}
