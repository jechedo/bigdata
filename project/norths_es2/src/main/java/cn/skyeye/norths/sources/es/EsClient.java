package cn.skyeye.norths.sources.es;

import cn.skyeye.resources.ConfigDetail;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/20 15:19
 */
public class EsClient {
    private final Logger logger = Logger.getLogger(EsClient.class);

    private Map<String, Integer> hostAndPort;
    private String clusterName;

    private boolean sniff;
    private String pingTimeout;
    private boolean netty3;

    private TransportClient client;

    public EsClient(Map<String, String> confMap) {
        if(confMap == null) confMap = Maps.newHashMap();
        ConfigDetail conf = new ConfigDetail(confMap);
        readConf(conf);

        Map<String, String> configMapWithPrefix = conf.getConfigMapWithPrefix("client.");
        Properties properties = new Properties();
        properties.putAll(configMapWithPrefix);
        buildClient(properties);
    }

    private void readConf(ConfigDetail conf) {
        this.hostAndPort = Maps.newHashMap();

        StringBuilder sb = new StringBuilder("es client的参数如下：");
        this.clusterName = conf.getConfigItemValue("client.clustername", "es");
        sb.append("\n\tclustername = ").append(clusterName);

        Set<String> servers = conf.getConfigItemSet("client.servers");
        if(servers.isEmpty())servers.add("localhost:9300");
        try {
            String[] kv;
            for (String server : servers){
                kv = server.split(":");
                if(kv.length == 2){
                    this.hostAndPort.put(kv[0], Integer.parseInt(kv[1]));
                }
            }
            sb.append("\n\tclient.servers = ").append(servers);
        } catch (NumberFormatException e) {
            logger.error("解析client.servers失败。", e);
        }

        this.sniff = conf.getConfigItemBoolean("client.transport.sniff", false);
        sb.append("\n\tclient.transport.sniff = ").append(sniff);

        this.pingTimeout = conf.getConfigItemValue("client.transport.ping_timeout", "120s");
        if(!pingTimeout.endsWith("s")){
            pingTimeout = pingTimeout + "s";
        }
        sb.append("\n\tclient.transport.ping_timeout = ").append(pingTimeout);

        this.netty3 = conf.getConfigItemBoolean("client.transport.netty3", false);
        sb.append("\n\tclient.transport.netty3 = ").append(netty3);

        logger.info(sb);
    }

    private void buildClient(Properties properties) {
        try {

            Settings.Builder builder = Settings.builder()
                    .put("client.transport.sniff", sniff)
                    .put("client.transport.ping_timeout", pingTimeout)
                    //.put("client.transport.ignore_cluster_name", true) // 忽略集群名字验证, 打开后集群名字不对也能连接上
                    .put("cluster.name", clusterName);
            if(properties != null && !properties.isEmpty()){
                builder.put(properties);
            }

            if(netty3){
                builder.put("transport.type","netty3")
                        .put("http.type", "netty3");
            }

            Settings settings = builder.build();
            this.client = TransportClient.builder().settings(settings).build();

            Set<Map.Entry<String, Integer>> entries = hostAndPort.entrySet();

            for (Map.Entry<String, Integer> entry : entries) {
                client.addTransportAddress(
                        new InetSocketTransportAddress(InetAddress.getByName(entry.getKey()), entry.getValue()));
            }
        } catch (Exception e) {
            logger.error("构建es的客户端失败。", e);
        }
    }

    public TransportClient getClient() {
        return client;
    }

    public Map<String, Integer> getHostAndPort() {
        return hostAndPort;
    }

    public String getClusterName() {
        return clusterName;
    }

    public static void main(String[] args) {
        Map<String, String> conf = Maps.newHashMap();
        conf.put("client.servers", "172.24.66.192:9300");
        conf.put("client.clustername", "es");

        EsClient esClient = new EsClient(conf);
        GetIndexResponse getIndexResponse = esClient.client.admin().indices().prepareGetIndex().get();
        String[] indices = getIndexResponse.indices();
        System.out.println(Arrays.asList(indices));

        SearchRequestBuilder searchRequestBuilder = esClient.client.prepareSearch()
               // .setTypes("alarm_collection")
                .setSize(70)
                .setFrom(0)
                .setQuery(QueryBuilders.matchAllQuery());
        SearchResponse searchResponse = searchRequestBuilder.get();
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();
        System.out.println(totalHits);
        SearchHit[] hits1 = hits.hits();
        for(SearchHit hit : hits1){
            System.out.println( hit.index() + "/" + hit.getType() + " : " + hit.sourceAsMap());
        }
        System.out.println(hits1.length);


    }

}
