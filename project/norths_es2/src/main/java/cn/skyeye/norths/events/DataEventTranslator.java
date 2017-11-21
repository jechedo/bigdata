package cn.skyeye.norths.events;

import com.lmax.disruptor.EventTranslatorOneArg;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/21 14:51
 */
public class DataEventTranslator implements EventTranslatorOneArg<DataEvent, Map<String, Object>> {

    @Override
    public void translateTo(DataEvent event, long sequence, Map<String, Object> arg0) {
        String source = (String) arg0.get(DataEventDisruptor.NAME);
        arg0.remove(DataEventDisruptor.NAME);
        event.setRecord(arg0);
        event.setSource(source);

    }
}
