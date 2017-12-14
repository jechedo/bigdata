package cn.skyeye.cascade.rpc.managers.handlers;

import cn.skyeye.cascade.CascadeContext;
import cn.skyeye.cascade.nodes.NodeInfoDetail;
import cn.skyeye.cascade.nodes.NodeManeger;
import cn.skyeye.common.json.Jsons;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Description:
 *   注册相关信息处理
 * @author LiXiaoCong
 * @version 2017/12/14 11:20
 */
public class RegistMessageHandler extends MessageHandler {
    private final Logger logger = Logger.getLogger(RegistMessageHandler.class);

    public RegistMessageHandler(CascadeContext cascadeContext) {
        super(cascadeContext);
    }

    @Override
    public String handleMessage(Map<String, String> message) {
        NodeInfoDetail remoteNodeInfo = NodeManeger.createNodeInfoDetail(message);
        Map<String, String> res = null;
        if(remoteNodeInfo != null) {
            switch (remoteNodeInfo.getStatus()) {
                case 1:  //上级注册
                    res = handleSupervisor(remoteNodeInfo);
                    break;
                case 2:  //下级注册
                    res = handleSubordinate(remoteNodeInfo);
                    break;
            }
        }
        return res == null ? null : Jsons.obj2JsonString(res);
    }

    private Map<String, String> handleSubordinate(NodeInfoDetail remoteNodeInfo){
        //存在一个用户确认的流程
        Map<String, String> res = Maps.newHashMap();
        res.put("registrationStatus", "0");
        res.put("registrationMsg", "待完成");

        logger.info(String.format("收到下级注册请求，并处理完成。注册信息如下：\n\t %s", remoteNodeInfo));
        return res;
    }

    private Map<String, String> handleSupervisor(NodeInfoDetail remoteNodeInfo){
        NodeManeger nodeManeger = cascadeContext.getNodeManeger();
        Map<String, String> res;
        if(nodeManeger.hasSupNode()){
            //已经存在一个上级了 拒绝注册
            res = Maps.newHashMap();
            res.put("registrationStatus", "0");
            res.put("registrationMsg", "There is already a superior!");
            logger.warn(String.format("收到上级注册请求，已存在上级，拒绝注册，注册信息如下：\n\t %s", remoteNodeInfo));
        }else{
            nodeManeger.setSupNode(remoteNodeInfo);
            //反馈为下级
            res = nodeManeger.getLocalNodeInfo().getRegistMSG("2");
            res.put("registrationStatus", "1");
            logger.info(String.format("收到上级注册请求，并处理完成。上级信息如下：\n\t %s", remoteNodeInfo));
        }
        return res;
    }
}
