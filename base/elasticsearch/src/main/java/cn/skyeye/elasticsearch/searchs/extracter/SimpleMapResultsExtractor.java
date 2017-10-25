package cn.skyeye.elasticsearch.searchs.extracter;

import com.google.common.collect.Maps;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.SingleBucketAggregation;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.geobounds.GeoBounds;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.elasticsearch.search.aggregations.metrics.stats.extended.ExtendedStats;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;

import java.util.*;

/**
 * Created by Eliran on 27/12/2015.
 */
public class SimpleMapResultsExtractor {

    private final boolean includeType;
    private final boolean includeScore;
    private int currentLineIndex;

    public SimpleMapResultsExtractor(boolean includeScore, boolean includeType) {
        this.includeScore = includeScore;
        this.includeType = includeType;
        this.currentLineIndex = 0;
    }

    public List<Map<String,Object>> extractResults(Object queryResult) throws MapExtractorException {

        if(queryResult instanceof SearchHits){
            SearchHits searchHits = (SearchHits) queryResult;
            SearchHit[] hits = searchHits.getHits();
            return readSearchHitsForDocsList(hits);
        }

        if(queryResult instanceof Aggregations){

            List<Map<String,Object>> lines = new ArrayList<>();
            lines.add(new HashMap<String, Object>());

            handleAggregations((Aggregations) queryResult, lines);


            //todo: need to handle more options for aggregations:
            //Aggregations that inhrit from base
            //ScriptedMetric
            return lines;

        }
        return null;
    }

    private  void handleAggregations(Aggregations aggregations,
                                     List<Map<String,Object>> lines) throws MapExtractorException {

        if(allNumericAggregations(aggregations)){
            lines.get(this.currentLineIndex).putAll(createLineForNumericAggregations(aggregations));
            return;
        }

        //aggregations with size one only supported when not metrics.
        List<Aggregation> aggregationList = aggregations.asList();
        if(aggregationList.size() > 1){
            throw new MapExtractorException("currently support only one aggregation at same level (Except for numeric metrics)");
        }

        Aggregation aggregation = aggregationList.get(0);
        //we want to skip singleBucketAggregations (nested,reverse_nested,filters)
        if(aggregation instanceof SingleBucketAggregation){
            Aggregations singleBucketAggs = ((SingleBucketAggregation) aggregation).getAggregations();
            handleAggregations(singleBucketAggs, lines);
            return;
        }

        if(aggregation instanceof NumericMetricsAggregation){
            handleNumericMetricAggregation(lines.get(currentLineIndex), aggregation);
            return;
        }

        if(aggregation instanceof GeoBounds){
            handleGeoBoundsAggregation(lines, (GeoBounds) aggregation);
            return;
        }
        if(aggregation instanceof TopHits){
            //todo: handle this . it returns hits... maby back to normal?
            //todo: read about this usages
            // TopHits topHitsAggregation = (TopHits) aggregation;
        }

        if(aggregation instanceof MultiBucketsAggregation){
            MultiBucketsAggregation bucketsAggregation = (MultiBucketsAggregation) aggregation;
            String name = bucketsAggregation.getName();
            //checking because it can comes from sub aggregation again
            Collection<? extends MultiBucketsAggregation.Bucket> buckets = bucketsAggregation.getBuckets();

            //clone current line.
            Map<String,Object> currentLine = lines.get(this.currentLineIndex);
            Map<String,Object> clonedLine = Maps.newHashMap(currentLine);

            //call handle_Agg with current_line++
            boolean firstLine = true;
            for (MultiBucketsAggregation.Bucket bucket : buckets) {
                //each bucket need to add new line with current line copied => except for first line
                String key = bucket.getKeyAsString();
                if(firstLine){
                    firstLine = false;
                }
                else {
                    currentLineIndex++;
                    currentLine =  Maps.newHashMap(clonedLine);
                    lines.add(currentLine);
                }
                currentLine.put(name, key);
                handleAggregations(bucket.getAggregations(), lines);
            }
        }

    }

    private void handleGeoBoundsAggregation(List<Map<String,Object>> lines, GeoBounds geoBoundsAggregation) {
        String geoBoundAggName = geoBoundsAggregation.getName();

        Map<String,Object> line = lines.get(this.currentLineIndex);
        line.put(geoBoundAggName+".topLeft.lon", geoBoundsAggregation.topLeft().getLon());
        line.put(geoBoundAggName+".topLeft.lat", geoBoundsAggregation.topLeft().getLat());
        line.put(geoBoundAggName+".bottomRight.lon", geoBoundsAggregation.bottomRight().getLon());
        line.put(geoBoundAggName+".bottomRight.lat", geoBoundsAggregation.bottomRight().getLat());
        lines.add(line);
    }

    private Map<String, Object> createLineForNumericAggregations(Aggregations aggregations) throws MapExtractorException {
        Map<String, Object> line = Maps.newHashMap();
        List<Aggregation> aggregationList = aggregations.asList();
        for(Aggregation aggregation : aggregationList){
            handleNumericMetricAggregation(line, aggregation);
        }
        return line;
    }

    private void handleNumericMetricAggregation(Map<String, Object> line, Aggregation aggregation) throws MapExtractorException {
        String name = aggregation.getName();

        if(aggregation instanceof NumericMetricsAggregation.SingleValue){
            if(aggregation instanceof ValueCount){
                line.put(name, ((ValueCount) aggregation).getValue());
            }else if(aggregation instanceof Cardinality){
                line.put(name, ((Cardinality) aggregation).getValue());
            }else {
                line.put(name, ((NumericMetricsAggregation.SingleValue) aggregation).value());
            }
        }

        //todo:Numeric MultiValue - Stats,ExtendedStats,Percentile...
        else if(aggregation instanceof NumericMetricsAggregation.MultiValue){
            if(aggregation instanceof Stats) {
                Stats stats = (Stats) aggregation;
                line.put("count", stats.getCount());
                line.put("sum", stats.getSum());
                line.put("avg", stats.getAvg());
                line.put("min", stats.getMin());
                line.put("max", stats.getMax());
                if(aggregation instanceof ExtendedStats){
                    ExtendedStats extendedStats = (ExtendedStats) aggregation;
                    line.put("sumOfSquares", extendedStats.getSumOfSquares());
                    line.put("variance", extendedStats.getVariance());
                    line.put("stdDeviation", extendedStats.getStdDeviation());
                }
            }
            else if( aggregation instanceof Percentiles){
                Percentiles percentiles = (Percentiles) aggregation;
                line.put("1.0", percentiles.percentile(1.0));
                line.put("5.0", percentiles.percentile(5.0));
                line.put("25.0", percentiles.percentile(25.0));
                line.put("50.0", percentiles.percentile(50.0));
                line.put("75.0", percentiles.percentile(75));
                line.put("95.0", percentiles.percentile(95.0));
                line.put("99.0", percentiles.percentile(99.0));
            }
            else {
                throw new MapExtractorException("unknown NumericMetricsAggregation.MultiValue:" + aggregation.getClass());
            }
        }
        else {
            throw new MapExtractorException("unknown NumericMetricsAggregation" + aggregation.getClass());
        }
    }

    private boolean allNumericAggregations(Aggregations aggregations) {
        List<Aggregation> aggregationList = aggregations.asList();
        for(Aggregation aggregation : aggregationList){
            if(!(aggregation instanceof NumericMetricsAggregation)){
                return false;
            }
        }
        return true;
    }

    private List<Map<String,Object>> readSearchHitsForDocsList(SearchHit[] hits) {

        List<Map<String,Object>> docsAsList = new ArrayList<>();
        Map<String, Object> doc;
        Map<String, SearchHitField> fields;
        for(SearchHit hit : hits){
            doc = hit.sourceAsMap();

            fields = hit.getFields();
            for(SearchHitField searchHitField : fields.values()){
                doc.put(searchHitField.getName(),searchHitField.value());
            }
            if(this.includeScore){
                doc.put("_score", hit.score());
            }
            if(this.includeType){
                doc.put("_type",hit.type());
            }

            docsAsList.add(doc);
        }

        return docsAsList;
    }
}
