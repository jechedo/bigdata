package cn.skyeye.common.event.disruptor.demo;

import com.lmax.disruptor.WorkHandler;

public class MyEventWorker implements WorkHandler<MyEvent> {

	private int id;

	@Override
	public void onEvent(MyEvent event) throws Exception {
		System.out.println(Thread.currentThread().getName() + " -- " + event);
	}

	private void setId(int id){
		this.id = id;
	}
}