package cn.skyeye.aptrules.streams;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ioc2rules.rules.IndexKey;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
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
public class RecordHitedExtracter implements ProcessorSupplier {

    private final Logger logger = Logger.getLogger(RecordHitedExtracter.class);

    private ARConf arConf;

    public RecordHitedExtracter(ARConf arConf){
        this.arConf = arConf;
    }

    @Override
    public Processor get() {
        return new Processor<String, String>() {
            private ProcessorContext context;
            private KeyValueStore<IndexKey, HitedRecord> kvStore;

            @Override
            public void init(ProcessorContext context) {
                this.context = context;
                this.context.schedule(1000);
                this.kvStore = (KeyValueStore<IndexKey, HitedRecord>) this.context.getStateStore("hits");
            }

            @Override
            public void process(String key, String line) {
                if(key != null){
                    try {
                        String[] split = key.split("<->");
                        IndexKey indexKey = IndexKey.newByIndexKeyString(split[1]);

                        split = line.split("<->");
                        Map<String, Object> ruleMap = Jsons.toMap(split[0]);
                        VagueRule vagueRule = VagueRule.newByRuleMap(arConf, ruleMap);

                        Map<String, Object> record = Jsons.toMap(split[1]);

                        HitedRecord hitedRecord = new HitedRecord(record, vagueRule);
                        this.kvStore.put(indexKey, hitedRecord);

                    } catch (Exception e) {
                        logger.error(String.format("key = %s, value = %s", key, line), e);
                    }
                }
                this.context.commit();
            }

            @Override
            public void punctuate(long timestamp) {
                try (KeyValueIterator<IndexKey, HitedRecord> iter = this.kvStore.all()) {
                    KeyValue<IndexKey, HitedRecord> entry;
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

    public class HitedRecord{
        private Map<String, Object> record;
        private VagueRule vagueRule;

       HitedRecord(Map<String, Object> record, VagueRule vagueRule) {
            this.record = record;
            this.vagueRule = vagueRule;
        }

        public Map<String, Object> getRecord() {
            return record;
        }

        public VagueRule getVagueRule() {
            return vagueRule;
        }
    }
}
