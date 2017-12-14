package cn.skyeye.cascade.rpc.managers.handlers;

import cn.skyeye.cascade.CascadeContext;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/14 11:20
 */
public abstract class MessageHandler {
    private final Logger logger = Logger.getLogger(MessageHandler.class);

    protected CascadeContext cascadeContext;
    public MessageHandler(CascadeContext cascadeContext){
        this.cascadeContext = cascadeContext;
    }

    public abstract String handleMessage(Map<String, String> message);
}
