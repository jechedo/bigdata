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
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import info.ganglia.gmetric4j.gmetric.GMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

	public static void main(String[] args) throws IOException, InterruptedException {
		GMetric aGMetric = new GMetric("test", 8649, GMetric.UDPAddressingMode.UNICAST, 1);
		MetricRegistry registry = new MetricRegistry();
		GangliaReporter.Builder builder = GangliaReporter.forRegistry(registry).withTMax(60).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.SECONDS);
		GangliaReporter reporter = builder.build(aGMetric);

		reporter.start(5, TimeUnit.SECONDS);
		registry.register("demo.02", new MemoryUsageGaugeSet());
		registry.register("demo.03", new GarbageCollectorMetricSet());
		registry.register("demo.04", new ThreadStatesGaugeSet());

		while (true){
			System.out.println("***");
			Thread.sleep(1000);
		}

	}

}
