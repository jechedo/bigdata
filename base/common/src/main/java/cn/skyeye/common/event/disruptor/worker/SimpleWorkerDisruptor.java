package cn.skyeye.common.event.disruptor.worker;

import com.bh.d406.bigdata.common.event.disruptor.event.DisruptorEvent;
import com.bh.d406.bigdata.common.event.disruptor.event.DisruptorEventTranslator;
import com.bh.d406.bigdata.common.event.disruptor.EventDisruptor;
import com.google.common.collect.Lists;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Description:
 *     WorkProcessor
         另一类消费者是WorkProcessor。每个WorkProcessor也有一个Sequence，多个WorkProcessor还共享一个Sequence用于互斥的访问RingBuffer。
         一个消息被一个WorkProcessor消费，就不会被共享一个Sequence的其他WorkProcessor消费。这个被WorkProcessor共享的Sequence相当于尾指针。
 * @author LiXiaoCong
 * @version 2017/6/25 18:32
 */
public class SimpleWorkerDisruptor extends EventDisruptor {

    private EventHandlerGroup<DisruptorEvent> handlerGroup;
    private List<Worker> workers;

    public SimpleWorkerDisruptor(String name,
                                 int ringBufferSize,
                                 DisruptorEventTranslator translator,
                                 Logger logger) {
        super(name, ringBufferSize, translator, logger);
    }

    public void handleEventsWithWorkerPool(Worker ... workers){
        this.handlerGroup = disruptor.handleEventsWithWorkerPool(workers);
        this.workers = Lists.newArrayList(workers);
    }

    /**
     *  检查所有
     */
    @Override
    public void waitComplete() {
        if(workers != null){
            for (Worker worker : workers){
                worker.waitComplete(getCurrentBatch(worker.getEventId()), getTotalBatchSize(worker.getEventId()));
            }
        }

        logger.info(String.format("%s 一共产生了 %s 批数据， 总共 %s 条。",
                        name, translator.getCurrentBatchMap(), translator.getTotalBatchSizeMap()));
    }
}
