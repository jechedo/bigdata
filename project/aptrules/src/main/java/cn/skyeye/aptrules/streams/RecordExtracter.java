package cn.skyeye.aptrules.streams;

import cn.skyeye.common.json.Jsons;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Description:
 *   从kafka topic中将数据解析出来
 * @author LiXiaoCong
 * @version 2017/11/6 11:30
 */
public class RecordExtracter implements ProcessorSupplier {

    private final Logger logger = Logger.getLogger(AlarmRecordChecker.class);

    @Override
    public Processor get() {
        return new Processor<String, String>() {
            private ProcessorContext context;
            private KeyValueStore<String, Map<String, Object>> kvStore;

            @Override
            public void init(ProcessorContext context) {
                this.context = context;
                this.context.schedule(1000);
                this.kvStore = (KeyValueStore<String, Map<String, Object>>) this.context.getStateStore("records");
            }

            @Override
            public void process(String key, String line) {
                try {
                    Map<String, Object> record = Jsons.toMap(line);
                    String recordId = getRecordId(record);
                    this.kvStore.put(recordId, record);
                } catch (Exception e) {
                    logger.error(String.format("数据不是标准的json：\n %s", line), e);
                }
                this.context.commit();
            }

            @Override
            public void punctuate(long timestamp) {
                try (KeyValueIterator<String,  Map<String, Object>> iter = this.kvStore.all()) {
                    KeyValue<String, Map<String, Object>> entry;
                    while (iter.hasNext()) {
                        entry = iter.next();
                        context.forward(entry.key, entry.value);
                    }
                }
            }

            @Override
            public void close() { }
        };
    }

    /**
     * 根据记录获取其id  有去重的效果
     * @param record
     * @return
     */
    private String getRecordId(Map<String, Object> record){
        return "";
    }
}
