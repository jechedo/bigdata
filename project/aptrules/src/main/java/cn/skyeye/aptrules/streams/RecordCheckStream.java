package cn.skyeye.aptrules.streams;

import cn.skyeye.aptrules.ARConf;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.TopologyBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Properties;

/**
 * Description:
 *   告警检测流任务
 * @author LiXiaoCong
 * @version 2017/11/6 10:49
 */
public class RecordCheckStream {
    private final Logger logger = Logger.getLogger(RecordCheckStream.class);

    private ARConf arConf;
    private TopologyBuilder topologyBuilder;

    public RecordCheckStream(ARConf arConf){
        this.arConf = arConf;
    }

    private void initStream(){
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "skyeye");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "test:9092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        topologyBuilder = new TopologyBuilder();
        topologyBuilder.addSource("source", "test");

        topologyBuilder.addProcessor("extracting", new RecordExtracter(), "source");
        topologyBuilder.addProcessor("checking", new AlarmRecordChecker(null), "extracting");
        topologyBuilder.addStateStore(Stores.create("records").withStringKeys().withValues(Map.class).inMemory().disableLogging().build(), "extracting");
        topologyBuilder.addStateStore(Stores.create("hits").withStringKeys().withStringValues().inMemory().disableLogging().build(), "checking");

        topologyBuilder.addSink("sink", "alarm_hits", "checking");

    }

}
