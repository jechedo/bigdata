package cn.skyeye.common.event.disruptor;

import cn.skyeye.common.event.disruptor.event.DisruptorEvent;
import com.lmax.disruptor.ExceptionHandler;
import org.apache.log4j.Logger;

/**
 * Description:
 *   简单的异常事件处理器：
 *            将异常事件记录到日志
 * @author LiXiaoCong
 * @version 1.0
 * @date 2016/11/23 13:21
 */
public class DisruptorEventExceptionLogger implements ExceptionHandler<DisruptorEvent> {

    private Logger logger;

    public DisruptorEventExceptionLogger(Logger logger){
        if(logger != null){
            this.logger = logger;
        }else {
            this.logger = Logger.getLogger("DisruptorEventExceptionLogger");
        }
    }

    @Override
    public void handleEventException(Throwable ex, long sequence, DisruptorEvent event) {
        logger.error(event, ex);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        logger.error(ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        logger.error(ex);
    }
}
