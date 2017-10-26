package cn.skyeye.aptrules.alarms.stores;

import cn.skyeye.aptrules.alarms.Alarm;
import cn.skyeye.elasticsearch.ElasticsearchContext;
import cn.skyeye.elasticsearch.EsClient;
import cn.skyeye.elasticsearch.searchs.SearchCenter;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/25 18:54
 */
public class AlarmEsStore extends AlarmStore {

    private final Logger logger = Logger.getLogger(AlarmEsStore.class);

    private ElasticsearchContext elasticsearchContext;
    private SearchCenter esSearcher;
    private String index = "alarm_collection";
    private String type  = "alarm_collection";

    public AlarmEsStore() {
        super();
        this.elasticsearchContext = ElasticsearchContext.get();
        this.esSearcher = elasticsearchContext.getSearcher();
    }

    @Override
    public List<Alarm> getAlarmsInStore(String conditions, int maxSize) {

        String eql = String.format("select * from %s/%s where %s limit %s", index, type, conditions, maxSize);
        SearchHits searchHits = esSearcher.searchSQLForHits(null, eql);
        SearchHit[] hits = searchHits.getHits();
        List<Alarm> res = null;
        if(hits.length > 0){
            Map<String, Object> alarmData;
            res = Lists.newArrayListWithCapacity(hits.length);
            String timestamp;
            for(SearchHit hit : hits){
                alarmData = hit.sourceAsMap();
                timestamp = String.valueOf(alarmData.get("@timestamp"));
                res.add(Alarm.newAlarmByData(hit.getId(), timestamp, alarmData));
            }
        }
        return res;
    }

    @Override
    public void storeAlarm(Alarm alarm) {
        EsClient esClient = elasticsearchContext.getEsClient();
        try {
            esClient.insert(index, type, alarm.getId(), alarm.getAlarm());
        } catch (Exception e) {
            logger.error(String.format("存储告警表单到es失败：\n %s", alarm), e);
        }
    }

    @Override
    public boolean exist(String alarmId) {
        EsClient esClient = elasticsearchContext.getEsClient();
        boolean exist = false;
        try {
            esClient.exist(index, type, alarmId);
            exist = true;
        } catch (Exception e) {
            logger.error(String.format("根据es判断是否为相同告警失败：\n %s", alarmId), e);
        }
        return exist;
    }
}
