package cn.skyeye.aptrules.streams;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.alarms.Alarmer;
import cn.skyeye.aptrules.ioc2rules.rules.IndexKey;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.Stores;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/6 15:28
 */
public class RecordHitedStream extends ARKafkaStream {

    private Alarmer alarmer;

    public RecordHitedStream(ARConf arConf, Alarmer alarmer){
        super(arConf);
        this.alarmer = alarmer;
    }

    @Override
    protected void initTopologyBuilder(){

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "skyeye-repoter");

        topologyBuilder.addSource("source", "alarm_hits");
        topologyBuilder.addProcessor("extracting", new RecordHitedExtracter(arConf), "source");
        topologyBuilder.addProcessor("checking", new AlarmRecordRepoter(alarmer), "extracting");
        topologyBuilder.addStateStore(Stores.create("hits").withKeys(IndexKey.class).withValues(RecordHitedExtracter.HitedRecord.class).inMemory().disableLogging().build(), "extracting");
    }
}
