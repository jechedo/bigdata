package cn.skyeye.aptrules.streams;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ioc2rules.rules.Ruler;
import com.google.common.base.Preconditions;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.Stores;

import java.util.Map;

/**
 * Description:
 *   告警检测流任务
 * @author LiXiaoCong
 * @version 2017/11/6 10:49
 */
public class RecordCheckStream extends ARKafkaStream{

    private Ruler ruler;

    public RecordCheckStream(ARConf arConf, Ruler ruler){
        super(arConf);
        this.ruler = ruler;
    }

    @Override
    protected void initTopologyBuilder() {
        String topicStr = arConf.getConfigItemValue("ar.data.kafka.topics");
        Preconditions.checkNotNull(topicStr, "配置项ar.data.kafka.topics不能为空。");

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "skyeye-checker");
        topologyBuilder.addSource("source", topicStr.split(","));

        topologyBuilder.addProcessor("extracting", new RecordExtracter(), "source");
        topologyBuilder.addProcessor("checking", new AlarmRecordChecker(ruler), "extracting");
        topologyBuilder.addStateStore(Stores.create("records").withStringKeys().withValues(Map.class).inMemory().disableLogging().build(), "extracting");
        topologyBuilder.addStateStore(Stores.create("hits").withStringKeys().withStringValues().inMemory().disableLogging().build(), "checking");

        topologyBuilder.addSink("sink", "alarm_hits", "checking");
    }
}
