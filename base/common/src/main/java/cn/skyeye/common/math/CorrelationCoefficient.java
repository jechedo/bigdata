package cn.skyeye.common.math;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
 
/**
  * Description:
  * 
  * 	相关系数计算
  * 
  * 
  *  Date    2016-7-8 下午5:35:43   
  *                  
  * @author  LiXiaoCong
  * @version 1.0
  * @since   JDK 1.7
 */
public class CorrelationCoefficient {  
	
	private static final Logger LOG = LoggerFactory.getLogger(CorrelationCoefficient.class);
    
    public static double evaluate(List<Double> xArray, List<Double> yArray, int precision){
    	
    	 Preconditions.checkArgument(xArray.size() == yArray.size(), "xArray.size() != yArray.size()");
    	 LOG.info(String.format("xArray = %s, yArray = %s", xArray, yArray));
    	 
         double xAverage =  Maths.mean(xArray);
         double yAverage =  Maths.mean(yArray);
         
    	 double res = calcuteNumerator(xArray, xAverage, yArray, yAverage) / calculateDenominator(xArray, xAverage, yArray, yAverage);  
    	
    	 res = Maths.precision(res, precision);
    	 LOG.info(String.format("xArray = %s, yArray = %s, CorrelationCoefficient = %s", xArray, yArray, res));
		 return res;
    }
    
    private static double calcuteNumerator(List<Double> xArray,  double xAverage, List<Double> yArray, double yAverage){  
        
        double result =0.0;  
        for(int i=0; i < xArray.size(); i++){  
            result += (xArray.get(i) - xAverage) * (yArray.get(i) - yAverage);  
        }  
        return result;  
    }  
    
    private static double calculateDenominator(List<Double> xArray,  double xAverage, List<Double> yArray, double yAverage){  
        
        double xException = 0.0;  
        double yException = 0.0;  
        for(int i = 0; i < xArray.size(); i++){  
            xException += Math.pow(xArray.get(i) - xAverage, 2);  
            yException += Math.pow(yArray.get(i) - yAverage, 2);  
        }  
      
        return  Math.sqrt(xException * yException);  
    }  
}  