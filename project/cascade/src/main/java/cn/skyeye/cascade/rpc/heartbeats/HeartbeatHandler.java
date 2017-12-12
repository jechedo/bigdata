package cn.skyeye.cascade.rpc.heartbeats;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/12 18:12
 */
public class HeartbeatHandler {

    private Map<String, Long> heartbeatsTimeMap;

    public HeartbeatHandler(){
        this.heartbeatsTimeMap = Maps.newConcurrentMap();
    }


    public byte[] handle(Map<String, Object> heartbeat){
        return null;
    }

}
