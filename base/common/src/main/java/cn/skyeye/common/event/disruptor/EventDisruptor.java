package cn.skyeye.common.event.disruptor;


import cn.skyeye.common.event.disruptor.event.DisruptorEvent;
import cn.skyeye.common.event.disruptor.event.DisruptorEventFactory;
import cn.skyeye.common.event.disruptor.event.DisruptorEventTranslator;
import cn.skyeye.common.event.disruptor.event.EventEntry;
import com.google.common.base.Preconditions;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.log4j.Logger;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description:
 *
 *     WaitStrategy
         当消费者等待在SequenceBarrier上时，有许多可选的等待策略，不同的等待策略在延迟和CPU资源的占用上有所不同，可以视应用场景选择：
         BusySpinWaitStrategy ： 自旋等待，类似Linux Kernel使用的自旋锁。低延迟但同时对CPU资源的占用也多。
         BlockingWaitStrategy ： 使用锁和条件变量。CPU资源的占用少，延迟大。
         SleepingWaitStrategy ： 在多次循环尝试不成功后，选择让出CPU，等待下次调度，多次调度后仍不成功，尝试前睡眠一个纳秒级别的时间再尝试。
                                 这种策略平衡了延迟和CPU资源占用，但延迟不均匀。
         YieldingWaitStrategy ： 在多次循环尝试不成功后，选择让出CPU，等待下次调。平衡了延迟和CPU资源占用，但延迟也比较均匀。
         PhasedBackoffWaitStrategy ： 上面多种策略的综合，CPU资源的占用少，延迟大。

     BatchEventProcessor
        在Disruptor中，消费者是以EventProcessor的形式存在的。其中一类消费者是BatchEvenProcessor。每个BatchEvenProcessor有一个Sequence，
        来记录自己消费RingBuffer中消息的情况。所以，一个消息必然会被每一个BatchEvenProcessor消费。

     WorkProcessor
         另一类消费者是WorkProcessor。每个WorkProcessor也有一个Sequence，多个WorkProcessor还共享一个Sequence用于互斥的访问RingBuffer。
         一个消息被一个WorkProcessor消费，就不会被共享一个Sequence的其他WorkProcessor消费。这个被WorkProcessor共享的Sequence相当于尾指针。

    WorkerPool
        共享同一个Sequence的WorkProcessor可由一个WorkerPool管理，这时，共享的Sequence也由WorkerPool创建。

 *
 * @author LiXiaoCong
 * @version 1.0
 * @date 2016/11/23 13:33
 */
public abstract class EventDisruptor {

    private static final int BUFFER_SIZE = 1024;

    protected String name;
    protected Disruptor<DisruptorEvent> disruptor;
    protected Logger logger;
    protected int bufferSize;

    protected final AtomicLong threadsCount = new AtomicLong(0);
    protected final DisruptorEventTranslator translator;
    /**Disruptor是否启动标示，只能启动一次**/
    private final AtomicBoolean started = new AtomicBoolean(false);

    protected EventDisruptor(String name,
                           DisruptorEventTranslator translator){
        this(name, BUFFER_SIZE, translator, null);
    }

    protected EventDisruptor(String name,
                           DisruptorEventTranslator translator,
                           Logger logger){
        this(name, BUFFER_SIZE, translator, logger);
    }

    protected EventDisruptor(String name,
                           int ringBufferSize,
                           DisruptorEventTranslator translator,
                           Logger logger){

        Preconditions.checkNotNull(name, "参数name不能为空。");
        Preconditions.checkNotNull(translator, "参数DisruptorEventTranslator不能为空。");

        this.translator = translator;
        this.name = name;
        this.bufferSize = ringBufferSize;

        //初始化disruptor
        disruptor = new Disruptor<>(new DisruptorEventFactory(),
                ringBufferSize, new ThreadFactory() {
           @Override
           public Thread newThread(Runnable r) {
               return new Thread(r, "EventDisruptor-" + threadsCount.getAndIncrement());
           }
       }, ProducerType.SINGLE, new YieldingWaitStrategy());

        if(logger != null){
            this.logger = logger;
        }else {
            this.logger = Logger.getLogger("EventDisruptor");
        }

       disruptor.setDefaultExceptionHandler(new DisruptorEventExceptionLogger(logger));
    }

    public Disruptor<DisruptorEvent> getDisruptor() {
        return disruptor;
    }

    public synchronized void publishEvent(final EventEntry eventEntry) {
        while (!disruptor.getRingBuffer().hasAvailableCapacity(6)){
            logger.warn(String.format("Disruptor %s 的剩余缓存位置少于6， 等待...", name));
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {}
        }

        disruptor.publishEvent(translator, eventEntry);
    }

    public long getCurrentBatch(String id) {
        return translator.getCurrentBatchNumById(id);
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public long getTotalBatchSize(String id) {
        return  translator.getTotalBatchSizeById(id);
    }

    public void start() {
        if(started.compareAndSet(false, true)) {
            disruptor.start();
        }else {
           logger.warn("EventDisruptor 已经启动过了。");
        }
    }

    public void shutDown(){
        if (this.disruptor != null) {
            int n = 0;
            while (!disruptor.getRingBuffer().hasAvailableCapacity(BUFFER_SIZE)){
                logger.info(String.
                        format("Disruptor %s 还有 %s 个未消费的事件,  等待第 %s 次重试...",
                                name,
                                (disruptor.getBufferSize() - disruptor.getRingBuffer().remainingCapacity()), ++n));
                try {
                    TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException e) {}
            }
            this.disruptor.shutdown();
            this.disruptor = null;
            this.started.set(false);
        }

        waitComplete();
    }

    /**
     *
     *   检查并等待所有事件是否处理完成
     *
     */
    public abstract void waitComplete();
}
