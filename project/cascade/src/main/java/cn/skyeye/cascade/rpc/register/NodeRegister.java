package cn.skyeye.cascade.rpc.register;

import cn.skyeye.cascade.CascadeContext;
import cn.skyeye.cascade.nodes.NodeInfoDetail;
import cn.skyeye.cascade.nodes.NodeManeger;
import cn.skyeye.cascade.rpc.MessageType;
import cn.skyeye.cascade.rpc.RegistrationStatus;
import cn.skyeye.common.json.Jsons;
import cn.skyeye.rpc.netty.transfers.NettyTransferService;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Description:
 *   注册相关
 * @author LiXiaoCong
 * @version 2017/12/14 9:52
 */
public class NodeRegister {
    private final Logger logger = Logger.getLogger(NodeRegister.class);

    protected CascadeContext cascadeContext;
    protected NettyTransferService transferService;
    public NodeRegister(CascadeContext cascadeContext){
        this.cascadeContext = cascadeContext;
        this.transferService = this.cascadeContext.getTransferService();
    }

    public void registSubordinate(String targetIP, NodeInfoDetail localInfo) throws Exception {
        Map<String, String> registMSG = localInfo.getRegistMSG("2");
        registMSG.put("type", MessageType.register.name());
        String reponse = cascadeContext.sendJson(registMSG, targetIP, 5000);
    }

    public void registSupervisor(String targetIP, NodeInfoDetail localInfo) throws Exception{
        registSupervisor(targetIP, cascadeContext.getCascadeConf().getPort(), localInfo);
    }

    public void registSupervisor(String targetIP, int targetPort, NodeInfoDetail localInfo) throws Exception {
        Map<String, String> registMSG = localInfo.getRegistMSG("1");
        registMSG.put("type", MessageType.register.name());
        String reponse = cascadeContext.sendJson(registMSG, targetIP, targetPort,5000);

        Map<String, String> res = Jsons.toMap(reponse);
        String registration = res.get("registrationStatus");
        int statusID = Integer.parseInt(registration);
        RegistrationStatus registrationStatus = RegistrationStatus.getRegistrationStatus(statusID);
        switch (registrationStatus){
            case refuse:
                //
                break;
            case success:
                //下级信息入库
                NodeInfoDetail nodeInfoDetail = NodeManeger.createNodeInfoDetail(res);
                cascadeContext.getNodeManeger().addSubNode(nodeInfoDetail);

                //写数据库
                break;
        }


        System.err.println(reponse);
    }
}
