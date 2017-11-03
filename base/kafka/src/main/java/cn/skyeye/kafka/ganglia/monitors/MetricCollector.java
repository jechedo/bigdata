package cn.skyeye.kafka.ganglia.monitors;


import com.codahale.metrics.Metric;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/3 11:08
 */
public interface MetricCollector {
    Metric collect() throws Exception;
}
