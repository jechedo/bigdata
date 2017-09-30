/**
  * Copyright (c) 2016, jechedo All Rights Reserved.
  *
 */
package cn.skyeye.common.event;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Description:
 * 
 * date: 2016-5-13 上午11:33:07   
 *                  
 * @author  Created by LiXiaoCong
 * @version 1.0
 * @since JDK 1.7
 */
public class AsyncExecutor implements Executor{
	
	private ExecutorService executorService;
	
	public AsyncExecutor(int nThreads){
		executorService = Executors.newFixedThreadPool(nThreads);
	}
	public AsyncExecutor(){
		this(8);
	}

	public void execute(Runnable command) {
		executorService.execute(command);
	}
	
	public void close() {
		executorService.shutdown();
		executorService = null;
	}

	public String toString() {
	      return "AsyncExecutor --> ExecutorService";
	   }
}
