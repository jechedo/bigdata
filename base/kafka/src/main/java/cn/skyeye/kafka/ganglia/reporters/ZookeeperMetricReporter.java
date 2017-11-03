/**
 * 
 */
package cn.skyeye.kafka.ganglia.reporters;


import cn.skyeye.kafka.ganglia.metrics.ZookeeperMetrics;

import java.util.Map;
import java.util.Set;

/**
 * @author pcaparroy
 *
 */
public class ZookeeperMetricReporter extends BaseGangliaMetricReporter{

    public static final String METRIC_SEPARATOR = ".";

	private String metricPrefix;
	public ZookeeperMetricReporter(String gmondHost, int port, String metricPrefix) {
		super(gmondHost, port);
		this.metricPrefix = metricPrefix;
	}

	@Override
	public void report() {
		super.getReporter().report();
	}

	public void collect(ZookeeperMetrics zMetric) {

        StringBuffer metricPath = new StringBuffer();
        metricPath.append(metricPrefix)
                .append(METRIC_SEPARATOR)
                .append(zMetric.getDisplayName())
                .append(METRIC_SEPARATOR);

        String metricName;
        DecimalGauge gauge;
        Set<Map.Entry<String, Double>> entries = zMetric.getMetrics().entrySet();

        for(Map.Entry<String, Double> entry : entries){
             metricName = String.format("%s%s", metricPath, entry.getKey());
             gauge = (DecimalGauge)getRegistry().getGauges().get(metricName);
            if(gauge==null){
                gauge = new DecimalGauge(entry.getValue());
                getRegistry().register(metricName, gauge);
            }
            gauge.setValue(entry.getValue());
        }
	}


}
