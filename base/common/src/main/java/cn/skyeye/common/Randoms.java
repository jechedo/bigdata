/**
 * 
 */
package cn.skyeye.common;

import java.util.Random;

/**
 * Description:
 * 		可用于产生定长的随机数。
 * 
 * History：
 * =============================================================
 * Date                      Version        Memo
 * 2016-3-1上午10:47:47            1.0            Created by LiXiaoCong
 * 
 * =============================================================
 * 
 * Copyright 2015, 武汉白虹软件科技有限公司 。
 */

public class Randoms {
	
	private static final String format = "%0#d";
	private static final Random  RANDOM = new Random();
	
	
	/**
	  * description:   
	  * 
	  * @param length     需要的随数的长度
	  * @return
	  *         String
	  * 2016-3-1 上午11:02:31
	  * by LiXiaoCong
	 */
	public static String getRandomNumber( int length ){
		
		return getRandomNumber(RANDOM.nextInt(  (int)Math.pow(10, length ) ) , length  );
		
	}
	
	public static String getRandomNumber(int number , int length ){
		
		return String.format(format.replace("#", String.valueOf( length) ), number);
	}
	

}
