package cn.skyeye.common.math;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

	/**
	 * 求和
	 *
	 * @param arr
	 * @return
	 */
	public static double getSum(double[] arr) {
		double sum = 0;
		for (double num : arr) {
			sum += num;
		}
		return sum;
	}

	/**
	 * 求均值
	 *
	 * @param arr
	 * @return
	 */
	public static double getMean(double[] arr) {
		return getSum(arr) / arr.length;
	}

	/**
	 * 求众数
	 *
	 * @param arr
	 * @return
	 */
	public static double getMode(double[] arr) {
		Map<Double, Integer> map = new HashMap<Double, Integer>();
		for (int i = 0; i < arr.length; i++) {
			if (map.containsKey(arr[i])) {
				map.put(arr[i], map.get(arr[i]) + 1);
			} else {
				map.put(arr[i], 1);
			}
		}
		int maxCount = 0;
		double mode = -1;
		Iterator<Double> iter = map.keySet().iterator();
		while (iter.hasNext()) {
			double num = iter.next();
			int count = map.get(num);
			if (count > maxCount) {
				maxCount = count;
				mode = num;
			}
		}
		return mode;
	}

	/**
	 * 求中位数
	 *
	 * @param arr
	 * @return
	 */
	public static double getMedian(double[] arr) {
		double[] tempArr = Arrays.copyOf(arr, arr.length);
		Arrays.sort(tempArr);
		if (tempArr.length % 2 == 0) {
			return (tempArr[tempArr.length >> 1] + tempArr[(tempArr.length >> 1) - 1]) / 2;
		} else {
			return tempArr[(tempArr.length >> 1)];
		}
	}


	/**
	 * 求中列数
	 *
	 * @param arr
	 * @return
	 */
	public static double getMidrange(double[] arr) {
		double max = arr[0], min = arr[0];
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
			if (arr[i] < min) {
				min = arr[i];
			}
		}
		return (min + max) / 2;
	}

	/**
	 * 求四分位数
	 *
	 * @param arr
	 * @return 存放三个四分位数的数组
	 */
	public static double[] getQuartiles(double[] arr) {
		double[] tempArr = Arrays.copyOf(arr, arr.length);
		Arrays.sort(tempArr);
		double[] quartiles = new double[3];
		// 第二四分位数（中位数）
		quartiles[1] = getMedian(tempArr);
		// 求另外两个四分位数
		if (tempArr.length % 2 == 0) {
			quartiles[0] = getMedian(Arrays.copyOfRange(tempArr, 0, tempArr.length / 2));
			quartiles[2] = getMedian(Arrays.copyOfRange(tempArr, tempArr.length / 2, tempArr.length));
		} else {
			quartiles[0] = getMedian(Arrays.copyOfRange(tempArr, 0, tempArr.length / 2));
			quartiles[2] = getMedian(Arrays.copyOfRange(tempArr, tempArr.length / 2 + 1, tempArr.length));
		}
		return quartiles;
	}

	/**
	 * 求极差
	 *
	 * @param arr
	 * @return
	 */
	public static double getRange(double[] arr) {
		double max = arr[0], min = arr[0];
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
			if (arr[i] < min) {
				min = arr[i];
			}
		}
		return max - min;
	}

	/**
	 * 求四分位数极差
	 *
	 * @param arr
	 * @return
	 */
	public static double getQuartilesRange(double[] arr) {
		return getRange(getQuartiles(arr));
	}

	/**
	 * 求截断均值
	 *
	 * @param arr 求值数组
	 * @param p   截断量p，例如p的值为20，则截断20%（高10%，低10%）
	 * @return
	 */
	public static double getTrimmedMean(double[] arr, int p) {
		int tmp = arr.length * p / 100;
		double[] tempArr = Arrays.copyOfRange(arr, tmp, arr.length + 1 - tmp);
		return getMean(tempArr);
	}

	/**
	 * 求方差
	 *
	 * @param arr
	 * @return
	 */
	public static double getVariance(double[] arr) {
		double variance = 0;
		double sum = 0, sum2 = 0;
		for (int i = 0; i < arr.length; i++) {
			sum += arr[i];
			sum2 += arr[i] * arr[i];
		}
		variance = sum2 / arr.length - (sum / arr.length) * (sum / arr.length);
		return variance;
	}

	/**
	 * 求绝对平均偏差(AAD)
	 *
	 * @param arr
	 * @return
	 */
	public static double getAbsoluteAverageDeviation(double[] arr) {
		double sum = 0;
		double mean = getMean(arr);
		for (int i = 0; i < arr.length; i++) {
			sum += Math.abs(arr[i] - mean);
		}
		return sum / arr.length;
	}

	/**
	 * 求中位数绝对偏差(MAD)
	 *
	 * @param arr
	 * @return
	 */
	public static double getMedianAbsoluteDeviation(double[] arr) {
		double[] tempArr = new double[arr.length];
		double median = getMedian(arr);
		for (int i = 0; i < arr.length; i++) {
			tempArr[i] = Math.abs(arr[i] - median);
		}
		return getMedian(tempArr);
	}

	/**
	 * 求标准差
	 * @param arr
	 * @return
	 */
	public static double getStandardDevition(double[] arr) {
		double sum = 0;
		double mean = getMean(arr);
		for (int i = 0; i < arr.length; i++) {
			sum += Math.sqrt((arr[i] - mean) * (arr[i] - mean));
		}
		return (sum / (arr.length - 1));
	}

	/**
	 * 最小-最大规范化
	 *
	 * @param arr
	 * @return 规范化后的数组
	 */
	public static double[] minMaxNormalize(double[] arr) {
		// 拷贝数组
		double[] tempArr = Arrays.copyOf(arr, arr.length);
		// 找到最大值和最小值
		double max = tempArr[0], min = tempArr[0];
		for (int i = 0; i < tempArr.length; i++) {
			if (tempArr[i] > max) {
				max = tempArr[i];
			}
			if (tempArr[i] < min) {
				min = tempArr[i];
			}
		}
		// 规范化
		for (int i = 0; i < tempArr.length; i++) {
			tempArr[i] = (tempArr[i] - min) / (max - min);
		}
		return tempArr;
	}


	/**
	 * Z-score规范化
	 * @param arr
	 * @return 规范化后的数组
	 */
	public static double[] zScoreNormalize(double[] arr) {
		// 拷贝数组
		double[] tempArr = Arrays.copyOf(arr, arr.length);
		// 求均值
		double sum = 0;
		for (double num : tempArr) {
			sum += num;
		}
		double mean = sum / tempArr.length;
		// 求标准差
		double sum2 = 0;
		for (int i = 0; i < tempArr.length; i++) {
			sum2 += Math.sqrt((tempArr[i] - mean) * (tempArr[i] - mean));
		}
		double standardDivition = sum2 / (tempArr.length - 1);
		// 标准化
		for (int i = 0; i < tempArr.length; i++) {
			tempArr[i] = (tempArr[i] - mean) / standardDivition;
		}
		return tempArr;

	}

	/**
	 * 小数定标规范化
	 * @param arr
	 * @return 规范化后的数组
	 */
	public static double[] decimalsNormalize(double[] arr){
		// 拷贝数组
		double[] tempArr = Arrays.copyOf(arr, arr.length);
		// 找到最大值
		double max = tempArr[0];
		for (int i = 0; i < tempArr.length; i++) {
			if (tempArr[i] > max) {
				max = tempArr[i];
			}
		}
		// 确定j的值（j为使max(|v'|)<1的最小整数）
		int j = 0;
		while (Math.abs(max/Math.pow(10,j))>=1){
			j++;
		}
		// 规范化
		for (int i = 0; i < tempArr.length; i++) {
			tempArr[i] = tempArr[i] / Math.pow(10,j);
		}
		return tempArr;

	}


}
