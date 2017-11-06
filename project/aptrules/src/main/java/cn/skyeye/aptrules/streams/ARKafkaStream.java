package cn.skyeye.aptrules.streams;

import cn.skyeye.aptrules.ARConf;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.TopologyBuilder;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/6 16:35
 */
public abstract class ARKafkaStream {
    protected final Logger logger = Logger.getLogger(ARKafkaStream.class);

    protected ARConf arConf;
    protected TopologyBuilder topologyBuilder;
    protected Properties props;

    private KafkaStreams streams;

    public ARKafkaStream(ARConf arConf){
        this.arConf = arConf;
        this.topologyBuilder = new TopologyBuilder();
        initBaseProp();
    }

    private void initBaseProp(){
        String bs = arConf.getConfigItemValue("ar.data.kafka.bootstrap.servers", "localhost:9092");

        props = new Properties();
        //props.put(StreamsConfig.APPLICATION_ID_CONFIG, "skyeye-checker");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bs);
        //props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    }

    protected abstract void initTopologyBuilder();

    public void start(){
        initTopologyBuilder();
        streams = new KafkaStreams(topologyBuilder, props);
        streams.start();
    }

    public KafkaStreams getStreams() {
        return streams;
    }

    public void close(){
        if(streams != null){
            streams.close();
        }
    }
}
