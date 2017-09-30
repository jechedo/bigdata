package cn.skyeye.common;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Description:
 * 		线程池管理器   单例  
 *      默认创建                                        newCachedThreadPool ：创建一个可缓存的线程池
 *      可通过指定线程的数量来创建：newFixedThreadPool  ： 创建固定大小的线程池
 * 
 * History：
 * =============================================================
 * Date                      Version        Memo
 * 2016-1-12下午2:34:04            1.0            Created by LiXiaoCong
 * 
 * =============================================================
 * 
 * Copyright 2015, 武汉白虹软件科技有限公司 。
 */

public class ThreadPoolManager implements Serializable {

	private static final long serialVersionUID = 1465361469484903956L;
	
	//private static final Logger LOG = Logger.getLogger(ThreadPoolManager.class);
	
	public static final ThreadPoolManager threadPoolManager =  new ThreadPoolManager();

	private static ThreadPoolManager tpm;
	
	private transient ExecutorService newCachedThreadPool;
	private transient ExecutorService newFixedThreadPool;

	private int poolCapacity;
	
	
	private ThreadPoolManager(){
		if( newCachedThreadPool == null  )
		newCachedThreadPool = Executors.newCachedThreadPool();
	}


	/**
	  * description:返回   newCachedThreadPool 
	  * @return
	  *         ExecutorService
	  * 2016-1-12 下午3:16:26
	  * by LiXiaoCong
	 */
	public ExecutorService getExecutorService(){
		
		if( newCachedThreadPool == null ){
			synchronized(ThreadPoolManager.class){
				if( newCachedThreadPool == null )
				newCachedThreadPool = Executors.newCachedThreadPool();
			}
		}
		return newCachedThreadPool;
	}
	
	
	/** 
	  * description:   返回   newFixedThreadPool
	  * @param poolCapacity
	  * @return
	  *         ExecutorService
	  * 2016-1-12 下午3:16:06
	  * by LiXiaoCong
	 */
	public ExecutorService getExecutorService(int poolCapacity){
		return getExecutorService(poolCapacity, false);
	}

	/**
	  * description:   返回   newFixedThreadPool
	  * @param poolCapacity
	  * @return
	  *         ExecutorService
	  * 2016-1-12 下午3:16:06
	  * by LiXiaoCong
	 */
	public synchronized ExecutorService getExecutorService(int poolCapacity, boolean closeOld){

		if(newFixedThreadPool == null
				|| (this.poolCapacity != poolCapacity)){

			if(newFixedThreadPool != null && closeOld){
				newFixedThreadPool.shutdown();
			}

			newFixedThreadPool = Executors.newFixedThreadPool(poolCapacity);
			this.poolCapacity = poolCapacity;
		}
		return newFixedThreadPool;
	}

}
