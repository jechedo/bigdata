package cn.skyeye.cascade.rpc.heartbeats;

import cn.skyeye.cascade.CascadeContext;
import cn.skyeye.cascade.nodes.NodeInfoDetail;
import cn.skyeye.cascade.rpc.managers.handlers.MessageHandler;
import cn.skyeye.common.json.Jsons;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/14 16:13
 */
public class HeartbeatReceiver extends MessageHandler {


    public HeartbeatReceiver(CascadeContext cascadeContext){
        super(cascadeContext);
    }

    @Override
    public String handleMessage(Map<String, String> message) {
        Map<String, String> res = Maps.newHashMap();

        String id = message.get("id");
        NodeInfoDetail subNodeInfo = cascadeContext.getNodeManeger().getSubNodeInfo(id);
        if(subNodeInfo != null){
            cascadeContext.getHeartbeatManager().updateSubHeartbeatTime(id);
            subNodeInfo.setIp(message.get("ip"));
            subNodeInfo.setName(message.get("name"));
            subNodeInfo.setCity(message.get("city"));
            subNodeInfo.setProvince(message.get("province"));
            res.put("status", "ok");
            logger.info(String.format("收到心跳信息：\n\t %s", message));
        }else{
            res.put("status", "fail");
            logger.info(String.format("不识别的心跳信息：\n\t %s", message));
        }
        return Jsons.obj2JsonString(res);
    }
}
