package cn.skyeye.elasticsearch.searchs;

import cn.skyeye.elasticsearch.ElasticsearchContext;
import cn.skyeye.elasticsearch.EsClient;
import cn.skyeye.elasticsearch.searchs.extracter.MapExtractorException;
import cn.skyeye.elasticsearch.searchs.extracter.SimpleMapResultsExtractor;
import cn.skyeye.elasticsearch.sql5x.SearchDao;
import cn.skyeye.elasticsearch.sql5x.query.SqlElasticSearchRequestBuilder;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchCenter {

	private final Logger logger = Logger.getLogger(SearchCenter.class);

	public Map<String, Object> searchSQL(String clientId, String eql) {

		long t = System.currentTimeMillis();
		Map<String, Object> res = emptyResult();

		EsClient client = ElasticsearchContext.get().getEsClient(clientId);
		if (client != null) {
			SearchDao searchDao = new SearchDao(client.getClient());
			SqlElasticSearchRequestBuilder select = null;
			try {
				select = (SqlElasticSearchRequestBuilder) searchDao.explain(eql).explain();
			} catch (Exception e) {
				logger.error(String.format("在es集群 %s 中使用 sql %s查询失败。", clientId, eql), e);
			}

			if (select != null) {
				SearchResponse searchResponse = (SearchResponse) select.get();
				SearchHits hits = searchResponse.getHits();
				Aggregations aggregations = searchResponse.getAggregations();

				Object queryResult = (aggregations == null) ? hits : aggregations;
				SimpleMapResultsExtractor resultsExtractor = new SimpleMapResultsExtractor(false, false);
				List<Map<String, Object>> result;
				try {
					result = resultsExtractor.extractResults(queryResult);
					res.put("total", hits.getTotalHits());
					res.put("datas", result);
				} catch (MapExtractorException e) {
					logger.error(String.format("在es集群 %s 中使用 sql %s查询成功，结果解析失败。", clientId, eql), e);
				}
			}
		} else {
			logger.error(String.format("不存在指定 %s es集群 。", clientId));
		}
		logger.info(String.format("本次查询耗时： %s 毫秒", (System.currentTimeMillis() - t)));

		return res;
	}

	public SearchHits searchSQLForHits(String clientId, String eql) {

		long t = System.currentTimeMillis();
		EsClient client = ElasticsearchContext.get().getEsClient(clientId);
		if (client != null) {
			SearchDao searchDao = new SearchDao(client.getClient());
			SqlElasticSearchRequestBuilder select = null;
			try {
				select = (SqlElasticSearchRequestBuilder) searchDao.explain(eql).explain();
			} catch (Exception e) {
				logger.error(String.format("在es集群 %s 中使用 sql %s查询失败。", clientId, eql), e);
			}

			if (select != null) {
				SearchResponse searchResponse = (SearchResponse) select.get();
				return searchResponse.getHits();
			}
		} else {
			logger.error(String.format("不存在指定 %s es集群 。", clientId));
		}
		logger.info(String.format("本次查询耗时： %s 毫秒", (System.currentTimeMillis() - t)));

		return null;
	}

	private Map<String, Object> emptyResult() {
		HashMap<String, Object> map = new HashMap<>();
		map.put("total", 0);
		map.put("datas", Lists.newArrayList());
		return map;
	}
}
