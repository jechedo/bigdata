package cn.skyeye.norths.events;

import cn.skyeye.norths.NorthContext;
import cn.skyeye.resources.ConfigDetail;
import com.lmax.disruptor.EventHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/21 15:46
 */
public abstract class DataEventHandler implements EventHandler<DataEvent> {

    protected final Log logger = LogFactory.getLog(DataEventHandler.class);

    protected AtomicLong totalEvent = new AtomicLong(0);

    protected String name;
    protected String conf_preffix;
    protected NorthContext northContext;
    protected ConfigDetail configDetail;

    public DataEventHandler(String name){
        this.name = name;
        this.conf_preffix = String.format("norths.handler.%s.", name);
        this.northContext = NorthContext.get();
        Map<String, String> config = northContext.getNorthsConf().getConfigMapWithPrefix(conf_preffix);
        this.configDetail = new ConfigDetail(config);
    }

    @Override
    public void onEvent(DataEvent event, long sequence, boolean endOfBatch) throws Exception {
        if(isAccept(event)) {
            onEvent(event);
        }else {
            logger.debug(String.format("%s不处理此数据源%s:%s数据", name, event.getSource(), event.getType()));
        }
        totalEvent.incrementAndGet();
    }

    public abstract void onEvent(DataEvent event);

    public abstract boolean isAccept(DataEvent event);

    public void shutdown(long total){
        while (total > totalEvent.get()){
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {}
        }

        logger.info(String.format("数据总量为：%s, 处理数据量为：%s。", total, totalEvent.get()));
    }
}
