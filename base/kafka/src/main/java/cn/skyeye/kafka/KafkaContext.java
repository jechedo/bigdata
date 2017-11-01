package cn.skyeye.kafka;


import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;
import java.util.Properties;

/**
 * Description:
 *  读取配置 以及创建  Producer 和  ConsumerConnector
 * @author LiXiaoCong
 * @version 2017/6/8 13:16
 */
public class KafkaContext {

    private static volatile KafkaContext kafkaContext;
    private KafkaBaseConf kafkaBaseConf;

    //private TopicAdmin topicAdmin;

    private KafkaContext(boolean loadEnv) {
        this.kafkaBaseConf = new KafkaBaseConf(loadEnv);
        //this.topicAdmin = new TopicAdmin(prop);
    }

    public static KafkaContext get(){
        return get(true);
    }

    public static KafkaContext get(boolean loadEnv) {
        if(kafkaContext == null){
            synchronized (KafkaContext.class){
                if(kafkaContext == null){
                    kafkaContext = new KafkaContext(loadEnv);
                }
            }
        }
        return kafkaContext;
    }

    public <K,V> KafkaProducer<K, V> newProducer(Map<String, Object> confs){
        return newProducer(confs, null, null);
    }

    public <K,V> KafkaProducer<K, V> newProducer(Map<String, Object> confs,
                                                 Serializer<K> keySerializer,
                                                 Serializer<V> valueSerializer){
        Properties properties = kafkaBaseConf.newBaseProperties();
        properties.putAll(confs);
        return new KafkaProducer<>(properties, keySerializer, valueSerializer);
    }


    public <K, V> KafkaConsumer<K, V> newConsumer(Map<String, Object> confs){
        return newConsumer(confs, null, null);
    }

    public <K, V> KafkaConsumer<K, V> newConsumer(Map<String, Object> confs,
                                                  Deserializer<K> keyDeserializer,
                                                  Deserializer<V> valueDeserializer){
        Properties properties = kafkaBaseConf.newBaseProperties();
        properties.putAll(confs);
        return new KafkaConsumer<>(properties, keyDeserializer, valueDeserializer);
    }


    public static void close(Producer producer){
        if(producer != null)producer.close();
    }

    public static void close(Consumer consumer){
        if(consumer != null)consumer.close();
    }
}
