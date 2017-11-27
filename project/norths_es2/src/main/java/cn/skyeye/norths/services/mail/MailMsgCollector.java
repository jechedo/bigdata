package cn.skyeye.norths.services.mail;

import cn.skyeye.norths.events.DataEvent;
import cn.skyeye.norths.events.DataEventHandler;

/**
 * Description:
 *   告警日志收集器
 * @author LiXiaoCong
 * @version 2017/11/27 14:34
 */
public class MailMsgCollector extends DataEventHandler {

    public MailMsgCollector(String name) {
        super(name);
    }

    @Override
    public void onEvent(DataEvent event) {

    }

    @Override
    public boolean isAcceept(DataEvent event) {
        return false;
    }
}
