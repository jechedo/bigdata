package cn.skyeye.common.event.disruptor.demo;

import com.lmax.disruptor.EventHandler;

public class MyEventHandler2 implements EventHandler<MyEvent> {
    @Override
	public void onEvent(MyEvent event, long sequence, boolean endOfBatch) throws Exception {
		System.out.println(Thread.currentThread().getName() + "2 -- " + event + " -- " + sequence + " -- " + endOfBatch);
	}
}