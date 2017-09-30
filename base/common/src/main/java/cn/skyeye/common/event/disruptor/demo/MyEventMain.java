package cn.skyeye.common.event.disruptor.demo;

import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class MyEventMain {
    public static void main(String[] args) throws InterruptedException {

        //ExecutorService executorService = Executors.newFixedThreadPool(2);
	    int bufferSize = 1024;


/*	    EventDisruptor<MyEvent> disruptor = new EventDisruptor<>(new MyEventFactory(),
		bufferSize, executorService, ProducerType.SINGLE, new YieldingWaitStrategy());*/

		Disruptor<MyEvent> disruptor = new Disruptor<>(new MyEventFactory(),
				bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.SINGLE, new YieldingWaitStrategy());

	    disruptor.setDefaultExceptionHandler(new IgnoreExceptionHandler());

		//disruptor.handleEventsWith(new MyEventHandler2(),new MyEventHandler());

		disruptor.handleEventsWith(new MyEventHandler2()).then(new MyEventHandler());  //Pipeline

		RingBuffer<MyEvent> ringBuffer = disruptor.start();

		MyEventProducer producer = new MyEventProducer(ringBuffer);

		for (long i = 0; i < 10; i++) {
			producer.onData(i);
			Thread.sleep(100);// wait for task execute....
		}

		disruptor.shutdown();
		//ExecutorsUtils.shutdownAndAwaitTermination(executorService, 60, TimeUnit.SECONDS);

    }
}