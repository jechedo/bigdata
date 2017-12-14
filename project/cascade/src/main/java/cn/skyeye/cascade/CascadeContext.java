package cn.skyeye.cascade;

import cn.skyeye.cascade.nodes.NodeInfoDetail;
import cn.skyeye.cascade.nodes.NodeManeger;
import cn.skyeye.cascade.rpc.managers.FileDataManager;
import cn.skyeye.cascade.rpc.managers.JsonDataManager;
import cn.skyeye.rpc.netty.RpcContext;
import cn.skyeye.rpc.netty.transfers.NettyTransferService;
import org.apache.log4j.Logger;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/12 17:39
 */
public class CascadeContext {
    private final Logger logger = Logger.getLogger(CascadeContext.class);

    private NodeManeger nodeManeger;
    private CascadeConf cascadeConf;
    private RpcContext rpcContext;
    private NettyTransferService transferService;

    private CascadeContext(){
        this.cascadeConf = new CascadeConf();
        this.nodeManeger = new NodeManeger(this);
        this.rpcContext = RpcContext.get();

        initTransferService();
        //startServer();
    }

    private void initTransferService(){
        NodeInfoDetail localNodeInfo = nodeManeger.getLocalNodeInfo();
        this.transferService = (NettyTransferService) rpcContext.newTransferService("skyeye",
                        localNodeInfo.getIp(),
                        cascadeConf.getPort(),
                        new FileDataManager(),
                        new JsonDataManager());
    }

    public NodeManeger getNodeManeger() {
        return nodeManeger;
    }

    public CascadeConf getCascadeConf() {
        return cascadeConf;
    }

    public NettyTransferService getTransferService() {
        return transferService;
    }
}
