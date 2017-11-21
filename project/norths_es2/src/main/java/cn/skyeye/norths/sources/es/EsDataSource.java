package cn.skyeye.norths.sources.es;

import cn.skyeye.common.json.Jsons;
import cn.skyeye.norths.events.DataEventDisruptor;
import cn.skyeye.norths.sources.DataSource;
import cn.skyeye.resources.ConfigDetail;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/20 16:32
 */
public class EsDataSource extends DataSource{

    private EsClient esClient;
    private List<IndexType> indexTypes;
    private File tmpfile;
    private Map<String, Object> starts;
    private long flushInterval;
    //private int maxRecordBatch;

    private ReentrantLock lock = new ReentrantLock();

    private Timer timer;
    private String lastStatus;
    private DataEventDisruptor eventDisruptor;

    private ExecutorService threadPool;

    public EsDataSource(Map<String, String> conf, ExecutorService threadPool,  DataEventDisruptor eventDisruptor){
        ConfigDetail configDetail = new ConfigDetail(conf);
        getTmpFile(configDetail);
        getIndexTypes(configDetail);

        this.flushInterval = configDetail.getConfigItemLong("north.datasources.status.flush.interval",
                10 * 60 * 1000L);

        Map<String, String> configMap = configDetail.getConfigMap("norths.datasources.es.");
        this.esClient = new EsClient(configMap);

        /*int maxPoolSize = configDetail.getConfigItemInteger("north.datasources.es.max.fetch.threads",
                8);
        int poolSize = indexTypes.size();
        if(poolSize == 0)poolSize = 1;
        if(poolSize > maxPoolSize)poolSize = maxPoolSize;
        this.threadPool = Executors.newFixedThreadPool(poolSize); */
        this.threadPool = threadPool;
        this.eventDisruptor = eventDisruptor;

       /* this.maxRecordBatch = configDetail.getConfigItemInteger("north.datasources.es.max.fetch.records",
                100000);*/

        startAutoFlushStatus();
    }

    private void getTmpFile(ConfigDetail configDetail) {
        this.starts = Maps.newConcurrentMap();
        String tmpFileDir = configDetail.getConfigItemValue("north.datasources.tmpfile.dir",
                "/opt/work/web/xenwebsite/data/");
        File file = new File(tmpFileDir);
        if(!file.exists()){
            file.mkdirs();
        }

        this.tmpfile = new File(file, "es_delta.txt");
        if(tmpfile.exists()){
            try {
                String startTimeStr = FileUtils.readFileToString(tmpfile);
                if(StringUtils.isNotBlank(startTimeStr))
                    this.starts.putAll(Jsons.toMap(startTimeStr));
                logger.info(String.format("文件%s存在，内容为：\n\t %s", tmpfile, startTimeStr));
            } catch (Exception e) {
                logger.error(String.format("读取%s失败。", tmpfile), e);
            }
        }else{
            try {
                tmpfile.createNewFile();
                logger.info(String.format("新建文件%s", tmpfile));
            } catch (IOException e) {
                logger.error(String.format("新建%s失败。", tmpfile), e);
            }
        }
    }

    private void startAutoFlushStatus(){
        timer = new Timer("EsDataSourceStatusFlusher");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                HashMap<String, Object> stringLongHashMap = Maps.newHashMap(starts);
                String str = Jsons.obj2JsonString(stringLongHashMap);
                if(!str.equals(lastStatus)) {
                    try {
                        FileUtils.write(tmpfile, str, Charset.forName("UTF-8"), false);
                        lastStatus = str;
                        logger.info(String.format("更新%s成功，更新内容为：\n\t", tmpfile, str));
                    } catch (IOException e) {
                        logger.error(String.format("更新%s失败。", tmpfile), e);
                    }
                }else {
                    logger.info(String.format("%s内容无更新。", tmpfile));
                }
            }
        }, flushInterval, flushInterval);

        logger.info(String.format("EsDataSourceStatusFlusher启动成功，周期为：%ss", flushInterval/1000));
    }

    private void getIndexTypes(ConfigDetail configDetail) {
        this.indexTypes = Lists.newArrayList();
        Set<String> indexTypeStr = configDetail.getConfigItemSet("norths.datasources.es.index.types");
        String[] kv;
        IndexType indexType;
        String startField;
        for(String str : indexTypeStr){
            kv = str.split("/");
            if(kv.length == 2){
                indexType = new IndexType(kv[0], kv[1]);
                startField = configDetail.getConfigItemValue(String.format("norths.datasources.es.%s.deltafield", str),
                        "@timestamp");
                indexType.startField = startField;
                indexType.start = starts.get(indexType.getTmpKey());

                Set<String> configItemSet = configDetail.getConfigItemSet(String.format("norths.datasources.es.%s.includes", str));
                if(configItemSet.size() > 0){
                    indexType.includes = configItemSet.toArray(new String[configItemSet.size()]);
                }
               configItemSet = configDetail.getConfigItemSet(String.format("norths.datasources.es.%s.excludes", str));
                if(configItemSet.size() > 0){
                    indexType.excludes = configItemSet.toArray(new String[configItemSet.size()]);
                }

                indexTypes.add(indexType);
            }
        }
        if(indexTypes.isEmpty()){
            logger.warn("配置项norths.datasources.es.index.types中无可用的数据源。");
        }else {
            logger.info(String.format("es数据源有：\n\t %s", indexTypes));
        }
    }

    @Override
    public List<Map<String, Object>> readData() {
        this.lock.lock();
        try {
            long time = System.currentTimeMillis();
            List<Future<Long>> futures = Lists.newLinkedList();
            for(IndexType indexType : indexTypes){
                futures.add(threadPool.submit(new EsFetcher(indexType)));
            }

            long total = 0;
            for (Future<Long> future : futures){
                try {
                    total += (future.get());
                } catch (Exception e) {
                   logger.error(null, e);
                }
            }
            logger.info(String.format("*********读取增量数据成功，total = %s, 耗时：%ss。**********",
                    total, (System.currentTimeMillis() - time)/1000));
        } finally {
            this.lock.unlock();
        }
        return null;
    }

    private class EsFetcher implements Callable<Long>{
        private IndexType indexType;
        private TransportClient client;
        private EsFetcher(IndexType indexType){
            this.indexType = indexType;
           this.client = esClient.getClient();
        }

        @Override
        public Long call() throws Exception {
            long time = System.currentTimeMillis();
            SearchRequestBuilder requestBuilder = client.prepareSearch(indexType.index).setTypes(indexType.type);
            RangeQueryBuilder qb = QueryBuilders.rangeQuery(indexType.startField);
            if(indexType.start != null)
                qb.gt(indexType.start);
            SearchResponse searchResponse = requestBuilder.addSort(indexType.startField, SortOrder.DESC)
                    .setSize(1)
                    .setQuery(qb)
                    .setFetchSource(new String[]{indexType.startField}, null).get();
            SearchHits hits = searchResponse.getHits();
            long totalHits = hits.getTotalHits();
            if(totalHits <= 0) return 0L;

            /*
            if(totalHits > maxRecordBatch){
                logger.warn(String.format("%s中的新增的数据量为%s, 超过了单批最大值%s，分段处理，查询前%s条",
                        indexType, totalHits, maxRecordBatch, maxRecordBatch));
                requestBuilder = client.prepareSearch(indexType.index).setTypes(indexType.type);
                if(indexType.start != null)
                    qb.gt(indexType.start);
                searchResponse = requestBuilder.addSort(indexType.startField, SortOrder.ASC)
                        .setFrom(maxRecordBatch - 1)
                        .setSize(1)
                        .setQuery(qb)
                        .setFetchSource(new String[]{indexType.startField}, null).get();
                hits = searchResponse.getHits();
            }*/

            SearchHit maxHit = hits.getHits()[0];
            Object max = maxHit.sourceAsMap().get(indexType.startField);
            qb.lte(max);

            try {
                long res = searchData(totalHits, qb);
                logger.info(String.format("查询%s的数据成功: \n\t max = %s, resNum = %s, 耗时：%ss 。",
                        indexType, max, res, (System.currentTimeMillis() - time)/1000));
                indexType.start = max;
                starts.put(indexType.getTmpKey(), max);
            } catch (Exception e) {
               logger.error(String.format("查询%s的数据失败。", indexType), e);
            }

            return totalHits;
        }

        private long searchData(long total, RangeQueryBuilder qb){
            SearchRequestBuilder requestBuilder = client.prepareSearch(indexType.index).setTypes(indexType.type);
            long res = 0;
            if(total <= 10000){
                SearchResponse searchResponse = requestBuilder.setQuery(qb)
                        .setFrom(0)
                        .setSize((int) total)
                        .setFetchSource(indexType.includes, indexType.excludes)
                        .get();

                SearchHit[] hits = searchResponse.getHits().getHits();
                for(SearchHit hit : hits){
                    eventDisruptor.publishEvent(hit.sourceAsMap());
                    res += 1;
                }
            }else{
                logger.info(String.format("查询%s的数据量过多，采用scoll search。", indexType));
                //采用scoll
                SearchResponse scrollResp = requestBuilder.setSearchType(SearchType.SCAN)
                        .setScroll(new TimeValue(60000))
                        .setFetchSource(indexType.includes, indexType.excludes)
                        .setQuery(qb)
                        .setSize(1000).get();
                while (true) {
                    for (SearchHit hit : scrollResp.getHits().getHits()) {
                        eventDisruptor.publishEvent(hit.sourceAsMap());
                        res += 1;
                    }
                    scrollResp = client.prepareSearchScroll(scrollResp.getScrollId())
                            .setScroll(new TimeValue(60000))
                            .get();
                    if (scrollResp.getHits().getHits().length == 0) {
                        break;
                    }
                }
            }
            return res;
        }

    }

    private class IndexType{
        private String index;
        private String type;
        private String startField;
        private Object start;
        private String[] excludes;
        private String[] includes;

        private IndexType(String index, String type) {
            this.index = index;
            this.type = type;
        }

        public String getIndex() {
            return index;
        }

        public String getType() {
            return type;
        }

        public String getStartField() {
            return startField;
        }

        public Object getStartStart() {
            return start;
        }

        public String getTmpKey(){
            return String.format("%s/%s/%s", index, type, startField);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("IndexType{");
            sb.append("index='").append(index).append('\'');
            sb.append(", type='").append(type).append('\'');
            sb.append(", startField='").append(startField).append('\'');
            sb.append(", start=").append(start);
            sb.append('}');
            return sb.toString();
        }
    }

    public static void main(String[] args) {

        HashMap<String, String> conf = Maps.newHashMap();
        conf.put("norths.datasources.es.index.types", "audit/audit");
        conf.put("norths.datasources.es.audit/audit.deltafield", "@timestamp");
        conf.put("norths.datasources.es.audit/audit.excludes", "detail,role_id");
        conf.put("north.datasources.es.max.fetch.records", "47");
        conf.put("norths.datasources.es.client.servers", "172.24.66.192:9300");
        conf.put("norths.datasources.es.client.clustername", "es");

       /* EsDataSource esDataSource = new EsDataSource(conf);
        List<Map<String, Object>> data = esDataSource.readData();
        data.forEach(System.out::println);*/
    }

}
