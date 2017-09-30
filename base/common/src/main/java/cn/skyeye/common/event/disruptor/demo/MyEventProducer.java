package cn.skyeye.common.event.disruptor.demo;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

public class MyEventProducer {

    private RingBuffer<MyEvent> ringBuffer;

	public MyEventProducer(RingBuffer<MyEvent> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	private static final EventTranslatorOneArg TRANSLATOR = new EventTranslatorOneArg<MyEvent, Long>() {
		@Override
		public void translateTo(MyEvent event, long sequence, Long value) {
			event.setValue(value);
		}
	};

	public void onData(final Long value) {
		ringBuffer.publishEvent(TRANSLATOR,value);
	}
}