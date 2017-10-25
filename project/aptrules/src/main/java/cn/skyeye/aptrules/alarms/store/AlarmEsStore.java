package cn.skyeye.aptrules.alarms.store;

import cn.skyeye.aptrules.alarms.Alarm;
import cn.skyeye.elasticsearch.ElasticsearchContext;
import cn.skyeye.elasticsearch.searchs.SearchCenter;
import org.elasticsearch.search.SearchHits;

import java.util.List;

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
    public List<Alarm> getAlarmsInStore(String key, int maxSize) {

        String eql = String.format("select * from %s/%s where %s limit 1", index, type);
        SearchHits searchHits = esSearcher.searchSQLForHits(null, eql);

        return null;
    }
}
