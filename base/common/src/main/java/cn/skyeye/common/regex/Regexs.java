package cn.skyeye.common.regex;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Regexs {
	
	public static final String MOBILEPHONE       = "^((13[0-9])|(14[5-7])|(15[^4])|(17[0,8])|(18[0-3,5-9]))\\d{8}$";
	public static final String USERNAME          = "([\\w]|[-]|[\\u4e00-\\u9fa5]){4,20}";
	
	public static boolean isMobileNO( String mobiles ){
		
		Pattern p = Pattern.compile(MOBILEPHONE);
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}
	
	public static boolean isIDCard(String certNo){
		
		return IDCardUtil.isIDCard(certNo);
	}
	
	
	/**
	  * description: 
	  * 
	  * 				将各种格式的  Mac 字符串 格式化成  XX-XX-XX-XX-XX-XX的格式
	  * 
	  * @param mac
	  * @return
	  *         String
	  * 2016-3-8 上午3:13:07
	  * by LiXiaoCong
	 */
	public static String formatMacStr( String mac ){
		
		    StringBuilder sb = new StringBuilder();
		
		    /* 提取出所有的 字母和数字 */
	        Pattern  pattern=Pattern.compile("[a-zA-Z0-9]");  
	        Matcher  ma=pattern.matcher(mac);  
	        while(ma.find()){  
	           sb.append(ma.group());  
	        }  
	        
	        sb.insert(2, "-")
	          .insert(5, "-")
	          .insert(8, "-")
	          .insert(11, "-")
	          .insert(14, "-");
	        
	        return sb.toString().toUpperCase();
	        
	}
	
	/**
	  * 
	  * description:
	  *      
	  *      长度为  4 - 20 个字符  ， 一个中文为两个字符  ，  
	  *      可包含 中文   ， 数字  ， 字母  , 下划线  ,  减号
	  *      
	  * 		
	  * @param name
	  * @return
	  *         String
	  * 2016-4-8 上午10:38:52
	  * by LiXiaoCong
	 */
	public static boolean isUserName( String name ){
		
		if( StringUtils.isBlank(name) ) return false;
		
		return isCorrect( USERNAME , name );
		
	}
	
	public static boolean isCorrect(String rgx, String res){
		
	    Pattern p = Pattern.compile(rgx);
	    Matcher m = p.matcher(res);


	    
	    return m.matches();
	  }
	
	/**
	  * description:
	  * 
	  * 			使用正则表达式获取字符串中的所有英文单词或数字
	  * 
	  * @param str
	  * @return
	  *         String
	  * 2016-3-8 上午3:17:00
	  * by LiXiaoCong
	 */
	public static String getAllNumberAndWord( String str ){
		
		    StringBuilder sb = new StringBuilder();
		    
	        Pattern  pattern=Pattern.compile("[a-zA-Z0-9]");  
	        Matcher  ma=pattern.matcher(str);  
	        while(ma.find()){  
	        	sb.append( ma.group());  
	        }  
	        
	        return sb.toString();
	}

	/**
	 *
	 * description:检测经度
	 *
	 * @param auth
	 * @return boolean 2016-7-19 下午4:50:06 by Chenhailong
	 */
	public static final String LONGITUDE = "^-?(([1-9]\\d?)|(1[0-7]\\d)|180)(\\.\\d{1,6})?$";
	public static boolean isLONGITUDE(String str) {return Regular(str, LONGITUDE);}


	/**
	 *
	 * description:检测纬度
	 *
	 * @param auth
	 * @return boolean 2016-7-19 下午4:50:06 by Chenhailong
	 */
	public static final String LATITUDE = "^-?(([1-8]\\d?)|([1-8]\\d)|90)(\\.\\d{1,6})?$";
	public static boolean isLATITUDE(String str) {return Regular(str, LATITUDE);}

	/**
	 * 匹配是否符合正则表达式pattern 匹配返回true
	 *
	 * @param str
	 *            匹配的字符串
	 * @param pattern
	 *            匹配模式
	 * @return boolean
	 */
	private static boolean Regular(String str, String pattern) {
		if (null == str || str.trim().length() <= 0)
			return false;
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		return m.matches();
	}

	public static void main(String[] args) {
		HashMultimap<String,String> valueFilters = HashMultimap.create();
		valueFilters.put("type", "10300001");
		valueFilters.put("type", "10600001");

		String reg = String.format("%s%s", "#",
				Joiner.on(String.format("%s%s", "|", "#")).join(valueFilters.get("type")));

		//String reg = "10300001|10600001";
		//String reg = Joiner.on("|").join(valueFilters.get("type"));
		System.out.println(reg);
		System.out.println(isCorrect(reg, "#10600001"));
	}
}