package cn.skyeye.common.event.disruptor.event;

import com.lmax.disruptor.EventFactory;

/**
 * Description:
 *      etl的单批数据事件 工厂类
 * @author LiXiaoCong
 * @version 1.0
 * @date 2016/11/23 10:17
 */
public class DisruptorEventFactory implements EventFactory<DisruptorEvent> {

    public DisruptorEvent newInstance() {
        return new DisruptorEvent();
    }
}
