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
        event.setRecord(arg0);
    }
}
