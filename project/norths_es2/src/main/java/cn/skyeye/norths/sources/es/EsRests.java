package cn.skyeye.norths.sources.es;

/**
 * Description:
 * es rest api
 * @author LiXiaoCong
 * @version 2017/11/20 11:18
 */
public class EsRests {

    /*
    private final Logger logger = Logger.getLogger(EsRests.class);

    private ConfigDetail configDetail;

    private List<HttpHost> httpHosts;
    private List<Header> defaultHeaders;

    private RestClient restClient;

    private boolean enableSniffer;
    private Sniffer sniffer;

    public EsRests(Map<String, String> conf){
        this.configDetail = new ConfigDetail(conf);
        readConf();

        RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[httpHosts.size()]));
        builder.setMaxRetryTimeoutMillis(60000);
        if(defaultHeaders != null && defaultHeaders.size() > 0){
            builder.setDefaultHeaders(defaultHeaders.toArray(new Header[defaultHeaders.size()]));
        }

        RestClientFailureListener restClientFailureListener = new RestClientFailureListener();

        if(enableSniffer) {
            SniffOnFailureListener sniffOnFailureListener = new SniffOnFailureListener();
            restClientFailureListener.setSubFailureListener(sniffOnFailureListener);
            builder.setFailureListener(restClientFailureListener);
            this.restClient = builder.build();

            this.sniffer = Sniffer.builder(restClient).setSniffAfterFailureDelayMillis(60000).build();
            sniffOnFailureListener.setSniffer(sniffer);
        }else{
            builder.setFailureListener(restClientFailureListener);
            this.restClient = builder.build();
        }

    }

    private void readConf(){
        this.httpHosts = Lists.newArrayList();
        this.defaultHeaders = Lists.newArrayList();

        StringBuilder params = new StringBuilder("EsRestClient的参数如下：");

        Set<String> servers = configDetail.getConfigItemSet("es.client.servers");
        if(servers.isEmpty())servers.add("localhost:9200");
        try {
            String[] hostAndPort;
            for(String server : servers){
                hostAndPort = server.split(":");
                if (hostAndPort.length == 2){
                    httpHosts.add(new HttpHost(hostAndPort[0], Integer.parseInt(hostAndPort[1])));
                }
            }
            params.append("\n\tes.client.servers = ").append(servers);
        } catch (NumberFormatException e) {
            logger.error("解析es.client.servers失败。", e);
        }

        Set<String> handlers = configDetail.getConfigItemSet("es.client.http.handlers");
        String[] kv;
        for(String handler : handlers){
            kv = handler.split(":");
            if(kv.length == 2){
                defaultHeaders.add(new BasicHeader(kv[0], kv[1]));
            }
        }
        params.append("\n\tes.client.http.handlers = ").append(handlers);

        this.enableSniffer = configDetail.getConfigItemBoolean("es.client.enable.sniffer", false);
        params.append("\n\tes.client.enable.sniffer = ").append(enableSniffer);

        logger.info(params);
    }

    public void  close(){
        try {
            if(sniffer != null){
                sniffer.close();
            }

            if(restClient != null){
                restClient.close();
            }
        } catch (IOException e) {
            logger.error(null, e);
        }
    }*/

}
