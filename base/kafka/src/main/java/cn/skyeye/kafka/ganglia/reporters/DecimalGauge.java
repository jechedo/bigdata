/**
 * 
 */
package cn.skyeye.kafka.ganglia.reporters;

import com.codahale.metrics.Gauge;

/**
 * @author pcaparroy
 *
 */
public class DecimalGauge implements Gauge<Double>{
	
	private Double value;

	public DecimalGauge(Double value) {
		super();
		this.value = value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Double getValue() {
		// TODO Auto-generated method stub
		return value;
	}

}
