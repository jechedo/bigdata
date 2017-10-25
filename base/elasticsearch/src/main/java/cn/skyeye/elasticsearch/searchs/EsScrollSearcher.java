package cn.skyeye.elasticsearch.searchs;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Map;
import java.util.Set;

/**
 * Description:
 *   es scroll 分页查询器
 *    scroll 适合于查询包含大量数据的索引的所有数据
 *    不存在跳页的情况， 只能从前往后 依次执行
 *    scroll每次返回的条数  是 索引的分片数 * size
 *
 *    非线程安全
 *    
 * @author LiXiaoCong
 * @version 2017/6/8 11:18
 */
public class EsScrollSearcher {

    private TransportClient client;
    private String[] indexs;
    private String[] types;

    private String[] includeFields;
    private String[] excludeFields;

    private QueryBuilder filter;
    private QueryBuilder query;
    private int size = 100;
    private Map<String, SortOrder> sortOrderMap;
    private int scrollOutTimeInMinutes = 6;

    private long total;
    private int shards;
    private SearchResponse response;
    private long searchTimes;


    public EsScrollSearcher(TransportClient client){
        this.client = client;
        this.sortOrderMap = Maps.newHashMap();
    }

    public  SearchHits next(){

        if(response == null){
            response = getFirstScollResponse();
        }else {
            response = client.prepareSearchScroll(response.getScrollId())
                             .setScroll(TimeValue.timeValueMinutes(scrollOutTimeInMinutes))
                             .execute().actionGet();
        }
        searchTimes++;
        return response.getHits();
    }

    private SearchResponse getFirstScollResponse(){

        SearchRequestBuilder requestBuilder = client.prepareSearch(indexs).setTypes(types)
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setFetchSource(includeFields, excludeFields)
                .setScroll(TimeValue.timeValueMinutes(scrollOutTimeInMinutes));

        if(filter != null)requestBuilder.setPostFilter(filter);
        if(query != null)requestBuilder.setQuery(query);
        requestBuilder.setSize(size);

        Set<Map.Entry<String, SortOrder>> entries = sortOrderMap.entrySet();
        for(Map.Entry<String, SortOrder> entry : entries) {
            requestBuilder.addSort(entry.getKey(), entry.getValue());
        }

        SearchResponse searchResponse = requestBuilder.get();
        this.total = searchResponse.getHits().getTotalHits();
        this.shards = searchResponse.getTotalShards();

        return searchResponse;
    }

    public String[] getIndexs() {
        return indexs;
    }

    public void setIndexs(String ... indexs) {
        this.indexs = indexs;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String ... types) {
        this.types = types;
    }

    public TransportClient getClient() {
        return client;
    }

    public QueryBuilder getQuery() {
        return query;
    }

    public void setQuery(QueryBuilder query) {
        this.query = query;
    }

    public QueryBuilder getFilter() {
        return filter;
    }

    public void setFilter(QueryBuilder filter) {
        this.filter = filter;
    }

    public void addSort(String key, SortOrder order){
        if(key != null && order != null)
        sortOrderMap.put(key, order);
    }

    public int getScrollOutTimeInMinutes() {
        return scrollOutTimeInMinutes;
    }

    public void setScrollOutTimeInMinutes(int scrollOutTimeInMinutes) {
        if(scrollOutTimeInMinutes > 0)this.scrollOutTimeInMinutes = scrollOutTimeInMinutes;
    }

    public long getTotal() {
        return total;
    }

    public int getShards() {
        return shards;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        Preconditions.checkArgument(size > 0, "size 必须大于0");
        this.size = size;
    }


    public String[] getIncludeFields() {
        return includeFields;
    }

    public void setIncludeFields(String[] includeFields) {
        this.includeFields = includeFields;
    }

    public String[] getExcludeFields() {
        return excludeFields;
    }

    public void setExcludeFields(String[] excludeFields) {
        this.excludeFields = excludeFields;
    }

    public long getSearchTimes() {
        return searchTimes;
    }
}
