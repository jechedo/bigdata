package cn.skyeye.elasticsearch;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;


/**
 * 根据配置文件构造es客户端
 * @author Hannibal
 */
public class EsClient{

    private final Logger logger = Logger.getLogger(EsClient.class);

    private String clientId;
    private Set<String> types;
    private Set<String> hosts;
    private int tcpPort;
    private String clusterName;

    private boolean sniff;
    private String pingTimeout;
    private boolean netty3;

    private TransportClient client;
    private volatile BulkProcessor bulkProcessor;

    EsClient(String clientId, EsBaseConf baseConf, Map<String, String> conf) {
       this.clientId = clientId;
       if(conf == null) conf = Maps.newHashMap();
       readConf(baseConf, conf);
       buildClient();
    }

    private void readConf(EsBaseConf baseConf, Map<String, String> conf) {

        StringBuilder sb = new StringBuilder();

        String item = String.format("es.%s.client.hosts", clientId);
        String  itemValue = conf.get(item);
        if(itemValue == null) itemValue = baseConf.getConfigItemValue(item, "localhost");
        hosts  = Sets.newHashSet(itemValue.split(","));
        sb.append("\n\t").append("hosts = ").append(hosts);

        item = String.format("es.%s.client.tcpport", clientId);
        itemValue = conf.get(item);
        if(itemValue == null) itemValue = baseConf.getConfigItemValue(item, "9300");
        try {
            tcpPort = Integer.parseInt(itemValue);
        } catch (NumberFormatException e) {
            logger.error(String.format("%s的tcpport配置%s不是标准的tcp端口，取默认值9300.", clientId, itemValue));
            tcpPort = 9300;
        }
        sb.append("\n\t").append("tcpPort = ").append(tcpPort);

        item = String.format("es.%s.client.clustername", clientId);
        itemValue = conf.get(item);
        if(itemValue == null) itemValue = baseConf.getConfigItemValue(item, "es");
        clusterName = itemValue;
        sb.append("\n\t").append("clustername = ").append(clusterName);

        item = String.format("es.%s.client.types", clientId);
        itemValue = conf.get(item);
        if(itemValue == null) itemValue = baseConf.getConfigItemValue(item);
        if(itemValue != null)types = Sets.newHashSet(itemValue.split(","));
        sb.append("\n\t").append("types = ").append(types);

        itemValue = conf.get("es.client.transport.sniff");
        if(itemValue == null) {
            this.sniff = baseConf.getConfigItemBoolean("es.client.transport.sniff", false);
        }else{
            this.sniff = "true".equalsIgnoreCase(itemValue) ? true : false;
        }
        sb.append("\n\t").append("sniff = ").append(sniff);

        this.pingTimeout = conf.get("es.client.transport.ping_timeout");
        if(pingTimeout == null){
            this.pingTimeout = baseConf.getConfigItemValue("es.client.transport.ping_timeout", "120s");
        }
        if(!pingTimeout.endsWith("s")){
            pingTimeout = pingTimeout + "s";
        }
        sb.append("\n\t").append("pingTimeout = ").append(pingTimeout);


        itemValue = conf.get("es.client.transport.netty3");
        if(itemValue == null) {
            this.netty3 = baseConf.getConfigItemBoolean("es.client.transport.netty3", false);
        }else{
            this.netty3 = "true".equalsIgnoreCase(itemValue) ? true : false;
        }
        sb.append("\n\t").append("netty3 = ").append(netty3);

        logger.info(String.format("客户端%s的配置如下：%s", clientId, sb));
    }

    private void buildClient() {
        try {
            Settings.Builder builder = Settings.builder()
                    .put("client.transport.sniff", sniff)
                    .put("client.transport.ping_timeout", pingTimeout)
                    //.put("client.transport.ignore_cluster_name", true) // 忽略集群名字验证, 打开后集群名字不对也能连接上
                    .put("cluster.name", clusterName);
            if(netty3){
                builder.put("transport.type","netty3")
                       .put("http.type", "netty3");
            }

            Settings settings = builder.build();
            this.client = new PreBuiltTransportClient(settings);

            for (String host : hosts) {
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), tcpPort));
            }
        } catch (Exception e) {
            logger.error(String.format("构建%s的客户端失败。", clientId), e);
        }
    }

    public synchronized BulkProcessor getBulkProcessor() {
        if(bulkProcessor == null){
            bulkProcessor = BulkProcessor.builder(
                    client,
                    new BulkProcessor.Listener() {
                        @Override
                        public void beforeBulk(long executionId,
                                               BulkRequest request) {
                        }

                        @Override
                        public void afterBulk(long executionId,
                                              BulkRequest request,
                                              BulkResponse response) {
                            if (response.hasFailures()) {
                                logger.error(String.format("executionId = %s, FailureMessage = %s",
                                        executionId, response.buildFailureMessage()));
                            }
                        }

                        @Override
                        public void afterBulk(long executionId,
                                              BulkRequest request,
                                              Throwable failure) {
                            logger.error(String.format("executionId = %s", executionId), failure);
                        }
                    })
                    .setName(clientId)
                    // 5k次请求执行一次bulk
                    .setBulkActions(5000)
                    // 1mb的数据刷新一次bulk
                    .setBulkSize(new ByteSizeValue(50, ByteSizeUnit.MB))
                    //固定60s必须刷新一次
                    .setFlushInterval(TimeValue.timeValueSeconds(60))
                    // 并发请求数量, 0不并发, 1并发允许执行
                    .setConcurrentRequests(1)
                    // 设置退避, 100ms后执行, 最大请求3次
                    .setBackoffPolicy(
                            BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                    .build();
        }
        return bulkProcessor;
    }

    public TransportClient getClient() {
        return client;
    }

    public String getClientId() {
        return clientId;
    }

    public Set<String> getHosts() {
        return hosts;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void insert(String index, String type, String id, Map<String, Object> record) throws Exception{

        if(record != null && !record.isEmpty()){
            IndexRequestBuilder builder = client.prepareIndex(index, type, id).setSource(record);
            IndexResponse indexResponse = builder.get();
            RestStatus status = indexResponse.status();

            switch (status){
                case OK:
                case CREATED:
                    logger.info(String.format("id为%s的数据写到到索引%s/%s成功。", id, index, type));
                    break;
            }
        }
    }

    public Map<String, Object> get(String index, String type, String id) throws Exception{
        GetResponse getResponse = client.prepareGet(index, type, id).get();
        return getResponse.getSourceAsMap();
    }

    public boolean exist(String index, String type, String id) throws Exception {
        GetResponse getResponse = client.prepareGet(index, type, id).get();
        return getResponse.isExists();
    }

}
