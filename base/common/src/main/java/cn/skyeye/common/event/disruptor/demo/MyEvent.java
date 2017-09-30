package cn.skyeye.common.event.disruptor.demo;

public class MyEvent {
    private long value;
	public void setValue(long value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "MyEvent{" +
				"value=" + value +
				'}';
	}
}
