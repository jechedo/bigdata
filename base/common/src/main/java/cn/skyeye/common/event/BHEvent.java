/**
  * Copyright (c) 2016, jechedo All Rights Reserved.
  *
 */
package cn.skyeye.common.event;

/**
 * Description:
 * 
 * 		
 * 
 *  Date    2016-6-6 上午9:48:23   
 *                  
 * @author  LiXiaoCong
 * @version 1.0
 * @since   JDK 1.7
 */
public class BHEvent extends Event{
	
	private final Object source;
	private final Object event;
	private final Long capTime;
	
	public BHEvent(Object source, Object event, Long capTime) {
		this.source = source;
		this.event = event;
		this.capTime = capTime;
	}
	
	public Object getSource() {
		return source;
	}
	public Object getEvent() {
		return event;
	}
	public Long getCapTime() {
		return capTime;
	}

	@Override
	public String toString() {
		return "BHEvent [source=" + source + ", event=" + event + ", capTime="
				+ capTime + "]";
	}
	
}
