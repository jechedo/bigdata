package cn.skyeye.elasticsearch;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/25 10:18
 */
public class ElasticsearchContext {
    private final Logger logger = Logger.getLogger(ElasticsearchContext.class);

    private volatile static ElasticsearchContext elasticsearchContext;

    private EsBaseConf baseConf;
    private Map<String, EsClient> clientMap;

    private ElasticsearchContext(boolean loadEnv){
        this.clientMap = Maps.newConcurrentMap();
        this.baseConf = new EsBaseConf(loadEnv);

        if(baseConf.isLoadOnStart() && baseConf.hasDefault()){
            List<String> clientIds = baseConf.getClientIds();
            for(String clientId : clientIds){
                initEsClient(clientId, null);
            }
        }
    }

    public static ElasticsearchContext get(){
        return  get(true);
    }

    public static ElasticsearchContext get(boolean loadEnv){
        if(elasticsearchContext == null){
            synchronized (ElasticsearchContext.class){
                if(elasticsearchContext == null){
                    elasticsearchContext = new ElasticsearchContext(loadEnv);
                }
            }
        }
        return elasticsearchContext;
    }

    public synchronized EsClient initEsClient(String clientId, Map<String, String> conf){
        EsClient esClient = clientMap.get(clientId);
        if(esClient == null){
            esClient = new EsClient(clientId, baseConf, conf);
            clientMap.put(clientId, esClient);
        }
        return esClient;
    }

    public EsClient getEsClient(String clientId){
        return clientMap.get(clientId);
    }

    public EsClient getEsClient(){
        return getEsClient(baseConf.getDefaultClientId());
    }
}
