package cn.skyeye.common.math;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Double.MAX_EXPONENT;
import static java.lang.Double.MIN_EXPONENT;
import static java.lang.Math.getExponent;

public class Maths {

	private static final String FORMAT = "%.#f";
	
	private Maths(){}
	
	/**
	  * description:
	  *
	  *		double类型保留  precision 位小数   四舍五入
	  *
	  * @param value
	  * @param precision
	  *
	  * Date    2016-7-8 下午5:46:55
	  * @author LiXiaoCong
	 */
	public static double precision(double value, int precision){
		return Double.parseDouble(String.format(FORMAT.replace("#", String.valueOf(precision)), value));
	}
	
	public static double mean(Iterable<? extends Number> values) {
		    return mean(values.iterator());
		  }
		
    public static double mean(Iterator<? extends Number> values) {
	   
	    checkArgument(values.hasNext(), "Cannot take mean of 0 values");
	    long count = 1;
	    double mean = checkFinite(values.next().doubleValue());
	    while (values.hasNext()) {
	      double value = checkFinite(values.next().doubleValue());
	      count++;
	      mean += (value - mean) / count;
	    }
	    return mean;
   }
	
    public static double checkFinite(double argument) {
	    checkArgument(isFinite(argument));
	    return argument;
    }
	  
    public static boolean isFinite(double d) {
	    return getExponent(d) <= MAX_EXPONENT;
	  }

	public  static boolean isNormal(double d) {
	    return getExponent(d) >= MIN_EXPONENT;
	 }
	
}
