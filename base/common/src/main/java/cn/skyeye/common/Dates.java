/**
 * 
 */
package cn.skyeye.common;

import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dates {

	private static Logger LOG = Logger.getLogger(Dates.class);
	
	private static final String DEFAULT_DATE_STR = "yyyy-MM-dd HH:mm:ss";
	
	 public static String nowStr(){
	    	
	    	return nowStr(new Date());
	  }
	 
	 public static String nowStr(Date date){
		 
		 return nowStr(date, DEFAULT_DATE_STR);
	 }
	 
	 public static String nowStr(Date date , String format){
		 
		 SimpleDateFormat df = new SimpleDateFormat(format);
		 
		 return df.format(date);
	 }

	 public static String long2Str(long time, String format){
		 return nowStr(new Date(time), format);
	 }

	 public static String long2Str(long time){
		 return long2Str(time, DEFAULT_DATE_STR);
	 }

	public static long str2long(String time, String format){
		Date date = null;
		try {
			date = str2Date(time, format);
		} catch (ParseException e) {
			LOG.error(null, e);
		}
		if(date != null){
			return date.getTime();
		}
		return -1;
	}

	public static Date str2Date(String time, String format) throws ParseException {
		return  new SimpleDateFormat(format).parse(time);
	}
	public static Date str2Date(String time) throws ParseException{
		return str2Date(time, DEFAULT_DATE_STR);
	}

	public static long str2long(String timeStr){
		return str2long(timeStr, DEFAULT_DATE_STR);
	}


	public static Date fuzzyTimeStr2long(String fuzzyTimeStr) throws IllegalArgumentException{

		StringBuilder sb = new StringBuilder();
		    /* 提取出所有的 字母和数字 */
		Pattern pattern=Pattern.compile("[0-9]");
		Matcher ma =pattern.matcher(fuzzyTimeStr);
		while(ma.find()){
			sb.append(ma.group());
		}
		int length = sb.length();
		if(length > 4) {
			Calendar calendar = Calendar.getInstance();
			switch (length){
				case 17 :
					calendar.set(Calendar.MILLISECOND,
							Integer.parseInt(sb.substring(14, 17)));
				case 16 :
					calendar.set(Calendar.MILLISECOND,
							Integer.parseInt(sb.substring(14, 16)));
				case 15 :
					calendar.set(Calendar.MILLISECOND,
							Integer.parseInt(sb.substring(14, 15)));

				case 14 :
					calendar.set(Calendar.SECOND,
							Integer.parseInt(sb.substring(12, 14)));

				case 12 :
					calendar.set(Calendar.MINUTE,
							Integer.parseInt(sb.substring(10, 12)));

				case 10 :
					calendar.set(Calendar.HOUR_OF_DAY,
							Integer.parseInt(sb.substring(8, 10)));
				case 8 :
					calendar.set(Calendar.DAY_OF_MONTH,
							Integer.parseInt(sb.substring(6, 8)));

				case 6 :
					calendar.set(Calendar.MONTH,
							Integer.parseInt(sb.substring(4, 6)) - 1);
				case 4 :
					calendar.set(Calendar.YEAR,
							Integer.parseInt(sb.substring(0, 4)));
					break;
				default:
					throw new IllegalArgumentException(String.format(
							"字符串 %s 的纯数字长度为 %s, 无法识别该长度的时间。", fuzzyTimeStr, length));
			}
			return calendar.getTime();
		}
		return null;
	}

	/**
	 * 返回两个日期直接间隔的月份。按 "yyyy-MM" 格式返回
	 * @param minDate 起始日期
	 * @param maxDate    结束日期
	 * @return
     */
	 public static List<String> getMonthBetween(Long minDate, long maxDate) {

		 List<String> result = null;
		 try {

			 result = getMonthBetween(minDate, maxDate, "yyyy-MM");

		 } catch (Exception e) {
			LOG.error(" minDate=" + minDate + ", maxDate=" + maxDate, e);
		}

		return result;
	 }

	/**
	 * 返回两个日期直接间隔的月份
	 * @param minDate 起始时间
	 * @param maxDate 结束时间
	 * @param format 日期格式 例如："yyyy-MM"
     * @return
     */
	public static List<String> getMonthBetween(Long minDate, long maxDate, String format) {

		List<String> result = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);//格式化为年月
			result = new ArrayList<String>();
			Calendar min = Calendar.getInstance();
			Calendar max = Calendar.getInstance();
			min.setTimeInMillis(minDate);
			min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);
			max.setTimeInMillis(maxDate);
			max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);

			Calendar curr = min;
			while (curr.before(max)) {
				result.add(sdf.format(curr.getTime()));
				curr.add(Calendar.MONTH, 1);
			}

		} catch (Exception e) {
			LOG.error(" minDate=" + minDate + ", maxDate=" + maxDate, e);
		}

		return result;
	}

	/**
	 * 获取两个日期之间中间的差值，根据format格式获取日期，field是要根据哪种格式获取日期，比如年，月，日，小时
	 * @param minDate
	 * @param maxDate
	 * @param format 时间格式 如：yyyy, yyyy-MM , yyyy-MM-dd, yyyy-MM-dd HH
	 * @param field 输入参数： Calendar.YEAR, Calendar.MONTH, Calendar.DATE, Calendar.HOUR
     * @return
     */
	public static List<String> getGapsBetween(Long minDate, long maxDate, String format, int field) {

		List<String> result = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);//格式化为年月
			result = new ArrayList<String>();
			Calendar min = Calendar.getInstance();
			Calendar max = Calendar.getInstance();
			min.setTimeInMillis(minDate);
			max.setTimeInMillis(maxDate);

			Calendar curr = min;
			while (curr.before(max)) {
				result.add(sdf.format(curr.getTime()));
				curr.add(field, 1);
			}

			String curDateStr = sdf.format(maxDate);
			if (!result.contains(curDateStr)) {
				result.add(curDateStr);
			}
		} catch (Exception e) {
			LOG.error(" minDate=" + minDate + ", maxDate=" + maxDate, e);
		}

		return result;
	}

}
