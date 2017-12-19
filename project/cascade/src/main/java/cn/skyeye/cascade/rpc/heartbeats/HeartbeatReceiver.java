package cn.skyeye.cascade.rpc.heartbeats;

import cn.skyeye.cascade.CascadeContext;
import org.apache.log4j.Logger;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/14 16:13
 */
public class HeartbeatReceiver {
    private final Logger logger = Logger.getLogger(HeartbeatReceiver.class);

    private CascadeContext cascadeContext;
    public HeartbeatReceiver(CascadeContext cascadeContext){
        this.cascadeContext = cascadeContext;
    }
}
