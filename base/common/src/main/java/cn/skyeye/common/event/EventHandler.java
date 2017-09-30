/**
  * Copyright (c) 2016, jechedo All Rights Reserved.
  *
 */
package cn.skyeye.common.event;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * 
 * 	事件处理的顶层抽象类
 * 
 * date: 2016-5-13 上午10:08:13   
 *                  
 * @author  Created by LiXiaoCong
 * @version 1.0
 * @since JDK 1.7
 */
public abstract class EventHandler<T extends Event> {
	
	protected Logger logger = Logger.getLogger(EventHandler.class);
	
	@Subscribe
    public abstract void parsing(T event);
    
    @Subscribe
    public void parsing(DeadEvent dead) {
    	logger.warn("DeadEvent : " + dead.getEvent());
    }
    
    public Map<String,Object> init(){
    	return new HashMap<String, Object>();
    }

    public void setLogger(Logger logger){
        this.logger = logger;
    }

    public void close(){}
}
