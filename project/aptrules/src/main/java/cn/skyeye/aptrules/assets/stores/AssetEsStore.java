package cn.skyeye.aptrules.assets.stores;

import cn.skyeye.aptrules.assets.Asset;
import cn.skyeye.elasticsearch.ElasticsearchContext;
import cn.skyeye.elasticsearch.searchs.SearchCenter;
import com.google.common.collect.Lists;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/26 14:54
 */
public class AssetEsStore extends AssetStore{

    private ElasticsearchContext elasticsearchContext;
    private SearchCenter esSearcher;
    private String index = "alarm_asset";
    private String type  = "alarm_asset";

    public AssetEsStore(){
        super();
        this.elasticsearchContext = ElasticsearchContext.get();
        this.esSearcher = elasticsearchContext.getSearcher();
    }

    @Override
    public List<Asset> queryAsset(String conditions, int maxSize) {

        String eql = String.format("select * from %s/%s where %s limit %s", index, type, conditions, maxSize);
        SearchHits searchHits = esSearcher.searchSQLForHits(null, eql);
        SearchHit[] hits = searchHits.getHits();
        List<Asset> res = null;
        if(hits.length > 0){
            res = Lists.newArrayListWithCapacity(hits.length);
            for(SearchHit hit : hits){
                res.add(new Asset(hit.getId(), hit.sourceAsMap()));
            }
        }
        return res;
    }

    @Override
    public Asset getByAssetId(String assetId) {
        Asset res = null;
        try {
            Map<String, Object> data = elasticsearchContext.getEsClient().get(index, type, assetId);
            if(data != null){
                res = new Asset(assetId, data);
            }
        } catch (Exception e) {
            logger.error(String.format("获取id为%s的Asset失败。", assetId), e);
        }

        return res;
    }

    @Override
    public void updateAssets(List<Asset> assets) {
        if(assets != null){
            try {
                TransportClient client = elasticsearchContext.getEsClient().getClient();
                BulkRequestBuilder bulk = client.prepareBulk();
                UpdateRequestBuilder updateBuilder;
                for(Asset asset : assets){
                    updateBuilder = client.prepareUpdate(index, type, asset.getId())
                            .setDoc(asset.getData())
                            .setUpsert(asset.getData());
                    bulk.add(updateBuilder);
                }

                BulkResponse bulkItemResponses = bulk.get();
                if(bulkItemResponses.hasFailures()){
                    logger.error(String.format("更新Assets:%s失败。\n %s", assets,
                            bulkItemResponses.buildFailureMessage()));
                }
            }catch (Exception e){
                logger.error(String.format("更新Assets:%s失败。", assets), e);
            }
        }
    }
}
