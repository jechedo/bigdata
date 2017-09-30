/**
  * Copyright (c) 2016, jechedo All Rights Reserved.
  *
 */
package cn.skyeye.common.event;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * Description:
 * 
 * 	关联事件总线
 * 
 *  Date    2016-6-6 上午10:00:14   
 *                  
 * @author  LiXiaoCong
 * @version 1.0
 * @since   JDK 1.7
 */
public class BHEventBus {
	
	private static final Logger LOG = LoggerFactory.getLogger(BHEventBus.class);
	
	private String busName;
	private boolean  useAsync;
	private AsyncExecutor executor;
	private final EventBus eventBus;
	
	private Map<String, EventHandler> handlers = Maps.newConcurrentMap();
	
	public BHEventBus(String busName){
		this(busName, true);
	}
	
	public BHEventBus(String busName, boolean useAsync){
		this.busName = busName;
		if(useAsync){
			this.useAsync = useAsync;
			executor = new AsyncExecutor();
			eventBus = new AsyncEventBus(busName, executor);
		}else{
			eventBus = new EventBus(busName);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void start(EventHandler ... eventHandlers){
		start(Lists.newArrayList(eventHandlers));
	}
	
	@SuppressWarnings("rawtypes")
	public void start(Collection<EventHandler> eventHandlers){
		String name = null;
		for(EventHandler handler : eventHandlers){
			name = handler.getClass().getName();
			if(!handlers.containsKey(name)){
				eventBus.register(handler);
				handlers.put(name, handler);
				LOG.info(String.format("注册事件处理器 %s成功。", name));
			}else{
				LOG.info(String.format("已存在需要注册的事件处理器 %s。", name));
			}
		}
	}
	
	
	public void post(Event event){
		eventBus.post(event);
		LOG.debug(String.format("发送事件 %s 成功 。", event.toString()));
	}
	
	public void post(Object source, Object event){
		post(source, event, null);
	}
	
	public void post(Object source, Object event, Long capTime){
		eventBus.post(new BHEvent(source, event, capTime));
	}
	
	public void stop(){
		
		if(executor != null){
			executor.close();
			executor = null;
		}

		for(Map.Entry<String, EventHandler> entry : handlers.entrySet()){
			entry.getValue().close();
		}
	}

	public String getBusName() {
		return busName;
	}

	public boolean isUseAsync() {
		return useAsync;
	}

}
