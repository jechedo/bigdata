package cn.skyeye.norths.events;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/21 14:41
 */
public class DataEventDisruptor {
    public static final String NAME = "DataEventDisruptor";

    private static final int BUFFER_SIZE = 2048;

    private final Log logger = LogFactory.getLog(DataEventDisruptor.class);

    private Disruptor<DataEvent> disruptor;
    private DataEventTranslator translator;

    private DataEventHandler[] handlers;

    private AtomicLong threadsCount = new AtomicLong(0);
    private AtomicBoolean started = new AtomicBoolean(false);

    private long totalEvent;

    public DataEventDisruptor(DataEventHandler... handlers){

        this.disruptor = new Disruptor<>(DataEvent::new,
                BUFFER_SIZE,
                r -> {
                    return new Thread(r,
                            "DataEventDisruptorThread-" + threadsCount.getAndIncrement());
                },
                ProducerType.SINGLE,
                new YieldingWaitStrategy());
        this.disruptor.setDefaultExceptionHandler(new ExceptionHandler<DataEvent>() {
            @Override
            public void handleEventException(Throwable ex, long sequence, DataEvent event) {
                logger.error(String.format("处理数据%s失败", event), ex);
            }

            @Override
            public void handleOnStartException(Throwable ex) {
                logger.error("初始化处理器失败", ex);
            }

            @Override
            public void handleOnShutdownException(Throwable ex) {
                logger.error("关闭异常。", ex);
            }
        });

        this.translator = new DataEventTranslator();
        this.disruptor.handleEventsWith(handlers);
        this.handlers = handlers;
    }

    public synchronized void publishEvent(String source, String type, Map<String, Object> record) {
        if(record != null) {
            while (!disruptor.getRingBuffer().hasAvailableCapacity(6)) {
                logger.warn("剩余缓存卡槽数小于6， 等待...");
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
            }
            record.put(NAME, new String[]{source, type});
            disruptor.publishEvent(translator, record);
            totalEvent += 1;
        }
    }

    public synchronized void start(){
        if(!started.get()) {
            this.disruptor.start();
            started.set(true);
            logger.info("disruptor启动成功。");
        }else {
            logger.warn("disruptor已经启动了。");
        }
    }

    public void shutDown(){

        if (this.disruptor != null) {
            int n = 0;
            while (!disruptor.getRingBuffer().hasAvailableCapacity(BUFFER_SIZE)){
                logger.info(String.format("还有 %s 个未消费的事件,  等待第 %s 次重试...",
                                (disruptor.getBufferSize() - disruptor.getRingBuffer().remainingCapacity()), ++n));
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {}
            }
            this.disruptor.shutdown();
            this.disruptor = null;
            this.started.set(false);
        }
           /*关闭所有的handler*/
        if(this.handlers != null){
            for (DataEventHandler handler : handlers){
                handler.shutdown(totalEvent);
            }
            this.handlers = null;
        }

        /*打印状态日志*/
        logger.info(
                String.format("关闭disruptor成功，处理了数据 %s 条。处理数据启动的线程总数为：%s",
                            totalEvent, threadsCount.get()));
    }
}
