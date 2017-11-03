/**
 * 
 */
package cn.skyeye.kafka.ganglia.reporters;
/**
 * @author pcaparroy
 *
 */


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ganglia.GangliaReporter;
import info.ganglia.gmetric4j.gmetric.GMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author pcaparroy
 *
 */
public  abstract class BaseGangliaMetricReporter implements MetricReporter{
	
	private static Logger LOG = LoggerFactory.getLogger(BaseGangliaMetricReporter.class);

	private GangliaReporter reporter;
	
	private MetricRegistry registry;

	public BaseGangliaMetricReporter(String gmondHost,int port) {
		if(gmondHost != null){
			try {
				GMetric aGMetric = new GMetric(gmondHost, port, GMetric.UDPAddressingMode.UNICAST, 1);
				registry = new MetricRegistry();
				GangliaReporter.Builder builder = GangliaReporter.forRegistry(registry).withTMax(60).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.SECONDS);
				reporter = builder.build(aGMetric);
			} catch (Throwable e) {
				LOG.warn("Failed to initialize GangliaReporter");
			}
		}
	}

	public GangliaReporter getReporter() {
		return reporter;
	}


	public MetricRegistry getRegistry() {
		return registry;
	}

	public abstract void report();

}
