/**
 * 
 */
package cn.skyeye.kafka.ganglia.reporters;


import cn.skyeye.kafka.ganglia.metrics.ZookeeperMetrics;

/**
 * @author pcaparroy
 *
 */
public interface MetricReporter {
	
	public static String METRIC_AGGREGATION_TYPE_AVERAGE = "METRIC_AGGREGATION_TYPE_AVERAGE";
    public static String METRIC_TIME_ROLLUP_TYPE_AVERAGE = "METRIC_TIME_ROLLUP_TYPE_AVERAGE";
    public static String METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL = "METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL";
    
    public static String METRIC_AGGREGATION_TYPE_OBSERVATION="METRIC_AGGREGATION_TYPE_OBSERVATION";
    public static String METRIC_TIME_ROLLUP_TYPE_CURRENT = "METRIC_TIME_ROLLUP_TYPE_CURRENT";
    public static String METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE = "METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE";

    public void collect(ZookeeperMetrics metric);
    
    
    public void report();
    
}
