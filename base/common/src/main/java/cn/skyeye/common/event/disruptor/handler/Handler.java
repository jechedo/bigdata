package cn.skyeye.common.event.disruptor.handler;


import cn.skyeye.common.event.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.EventHandler;
import org.apache.log4j.Logger;

/**
 * Description:
 *    根据  event的类型ID 处理数据
 * @author LiXiaoCong
 * @version 2017/6/25 19:02
 */
public abstract class Handler implements EventHandler<DisruptorEvent> {

    private Logger logger;

    protected String eventId;
    protected long totalDealSize;
    protected long currentBatch;


    public Handler(String eventId, Logger logger){
        this.eventId = eventId;
        if(logger != null) {
            this.logger = logger;
        }else {
            this.logger = Logger.getLogger("Handler");
        }
    }

    @Override
    public void onEvent(DisruptorEvent event, long sequence, boolean endOfBatch) throws Exception {
        if(event.getId().equalsIgnoreCase(eventId)) {
            logger.info(String.format("Handler执行第 %s 次 %s 的数据处理，记录数为：%s。",
                    ++currentBatch, eventId, event.getBatchSize()));

            execute(event);
            totalDealSize += event.getBatchSize();
            logger.info(String.format("Handler 执行第 %s 次 %s 的数据处理完成。",
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
