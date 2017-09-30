package cn.skyeye.common.event.disruptor.demo;

import com.lmax.disruptor.EventHandler;

public class MyEventHandler implements EventHandler<MyEvent> {
    @Override
	public void onEvent(MyEvent event, long sequence, boolean endOfBatch) throws Exception {
		System.out.println(Thread.currentThread().getName() + " -- " + event + " -- " + sequence + " -- " + endOfBatch);
	}
}