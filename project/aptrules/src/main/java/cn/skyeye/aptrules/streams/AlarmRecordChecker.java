package cn.skyeye.aptrules.streams;

import cn.skyeye.aptrules.ioc2rules.rules.IndexKey;
import cn.skyeye.aptrules.ioc2rules.rules.Ruler;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
import cn.skyeye.common.json.Jsons;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description:
 *   告警信息校验
 * @author LiXiaoCong
 * @version 2017/11/6 11:34
 */
public class AlarmRecordChecker implements ProcessorSupplier {
    private final Logger logger = Logger.getLogger(AlarmRecordChecker.class);

    private Ruler ruler;
    public AlarmRecordChecker(Ruler ruler){
        this.ruler = ruler;
    }

    @Override
    public Processor get() {
        return new Processor<String, Map<String, Object>>() {
            private ProcessorContext context;
            private KeyValueStore<String, String> hits;

            @Override
            public void init(ProcessorContext context) {
                this.context = context;
                this.context.schedule(1000 * 60 * 5L);
                this.hits = (KeyValueStore<String, String>) this.context.getStateStore("hits");
            }

            @Override
            public void process(String key, Map<String, Object> value) {
                Ruler.Hits hitRules = ruler.matchRules(value);
                if(hitRules.noEmpty()){
                    String recordJson = Jsons.obj2JsonString(value);
                    Set<Map.Entry<IndexKey, List<VagueRule>>> hitSet = hitRules.getHitSet();
                    hitSet.forEach(entry ->{
                        String indexKeyString = entry.getKey().getIndexKeyString();
                        List<VagueRule> rules = entry.getValue();
                        int size = rules.size();
                        String hitKey;
                        String hitValue;
                        for (int i = 0; i < size; i++) {
                            String ruleString = Jsons.obj2JsonString(rules.get(i).getRuleMap());
                            hitKey = String.format("%s<->%s", i, indexKeyString);
                            hitValue = String.format("%s<->%s", ruleString, recordJson);
                            this.hits.put(hitKey, hitValue);
                            logger.info(String.format("hitKey = %s, hitValue = %s", hitKey, hitValue));
                        }
                    });
                }
                this.context.commit();
            }

            @Override
            public void punctuate(long timestamp) {
                try (KeyValueIterator<String, String> iter = this.hits.all()) {
                    KeyValue<String, String> entry;
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
}
