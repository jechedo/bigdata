package cn.skyeye.common.event.disruptor.handler;


import cn.skyeye.common.event.disruptor.EventDisruptor;
import cn.skyeye.common.event.disruptor.event.DisruptorEvent;
import cn.skyeye.common.event.disruptor.event.DisruptorEventTranslator;
import com.google.common.collect.Lists;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Description:
 *     BatchEventProcessor
         在Disruptor中，消费者是以EventProcessor的形式存在的。其中一类消费者是BatchEvenProcessor。每个BatchEvenProcessor有一个Sequence，
         来记录自己消费RingBuffer中消息的情况。所以，一个消息必然会被每一个BatchEvenProcessor消费。
 * @author LiXiaoCong
 * @version 2017/6/25 18:32
 */
public class SimpleHandlerDisruptor extends EventDisruptor {

    protected EventHandlerGroup<DisruptorEvent> handlerGroup;
    protected List<Handler> handlers;

    public SimpleHandlerDisruptor(String name,
                                  int ringBufferSize,
                                  DisruptorEventTranslator translator,
                                  Logger logger) {
        super(name, ringBufferSize, translator, logger);
    }

    public void handleEventsWithHandler(Handler ... handlers){
        this.handlerGroup = disruptor.handleEventsWith(handlers);
        this.handlers = Lists.newArrayList(handlers);
    }

    /**
     *  检查所有
     */
    @Override
    public void waitComplete() {
        if(handlers != null){
            for (Handler handler : handlers){
                handler.waitComplete(getCurrentBatch(handler.getEventId()), getTotalBatchSize(handler.getEventId()));
            }
        }

        logger.info(String.format("%s 一共产生了 %s 批数据， 总共 %s 条。",
                        name, translator.getCurrentBatchMap(), translator.getTotalBatchSizeMap()));
    }
}
