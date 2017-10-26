package cn.skyeye.aptrules.alarms.stores;

import cn.skyeye.aptrules.alarms.Alarm;
import cn.skyeye.elasticsearch.ElasticsearchContext;
import cn.skyeye.elasticsearch.searchs.SearchCenter;
import com.google.common.collect.Lists;
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

    private SearchCenter esSearcher;
    private String index = "alarm_collection";
    private String type  = "alarm_collection";

    public AlarmEsStore() {
        super();
        this.esSearcher = ElasticsearchContext.get().getSearcher();
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
                res.add(Alarm.newAlarmByData(timestamp, alarmData));
            }
        }
        return res;
    }

    @Override
    public void storeAlarm(Alarm alarm) {}
}
