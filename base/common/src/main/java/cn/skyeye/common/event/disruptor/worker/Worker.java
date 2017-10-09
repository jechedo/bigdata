package cn.skyeye.common.event.disruptor.worker;

import cn.skyeye.common.event.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.WorkHandler;
import org.apache.log4j.Logger;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/6/25 19:02
 */
public abstract class Worker implements WorkHandler<DisruptorEvent> {

    private Logger logger;

    protected String eventId;
    protected long totalDealSize;
    protected long currentBatch;


    public Worker(String eventId, Logger logger){
        this.eventId = eventId;
        if(logger != null) {
            this.logger = logger;
        }else {
            this.logger = Logger.getLogger("Worker");
        }
    }

    @Override
    public void onEvent(DisruptorEvent event) throws Exception {
        if(event.getId().equalsIgnoreCase(eventId)) {
            logger.info(String.format("Worker执行第 %s 次 %s 的数据处理，记录数为：%s。",
                    ++currentBatch, eventId, event.getBatchSize()));

            execute(event);
            totalDealSize += event.getBatchSize();
            logger.info(String.format("Worker执行第 %s 次 %s 的数据处理完成。",
                    currentBatch, eventId));
        }
    }

    public String getEventId() {
        return eventId;
    }

    public abstract void execute(DisruptorEvent event);


    /**
     * 最后的一批  以及 需要处理的事件总数
     * 主要用于避免 在数据没有处理完 就关闭了 Disruptor 导致数据丢失
     * @param latestBatch
     * @param totalRows
     */
    public abstract void waitComplete(long latestBatch, long totalRows);

}
