package cn.skyeye.aptrules.streams;

import cn.skyeye.aptrules.alarms.Alarmer;
import cn.skyeye.aptrules.ioc2rules.rules.IndexKey;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.log4j.Logger;

/**
 * Description:
 *   告警信息校验
 * @author LiXiaoCong
 * @version 2017/11/6 11:34
 */
public class AlarmRecordRepoter implements ProcessorSupplier {
    private final Logger logger = Logger.getLogger(AlarmRecordRepoter.class);

    private Alarmer alarmer;
    public AlarmRecordRepoter(Alarmer alarmer){
        this.alarmer = alarmer;
    }

    @Override
    public Processor get() {
        return new Processor<IndexKey, RecordHitedExtracter.HitedRecord>() {
            private ProcessorContext context;

            @Override
            public void init(ProcessorContext context) {
                this.context = context;
                this.context.schedule(1000 * 60 * 5L);
            }

            @Override
            public void process(IndexKey key, RecordHitedExtracter.HitedRecord value) {
                alarmer.checkAndReportAlarm(value.getRecord(), key, value.getVagueRule());
                this.context.commit();
            }

            @Override
            public void punctuate(long timestamp) { }

            @Override
            public void close() { }
        };
    }
}
