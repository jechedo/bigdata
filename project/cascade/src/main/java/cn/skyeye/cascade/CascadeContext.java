package cn.skyeye.cascade;

import cn.skyeye.cascade.nodes.NodeInfoDetail;
import cn.skyeye.cascade.nodes.NodeManeger;
import cn.skyeye.cascade.rpc.managers.FileDataManager;
import cn.skyeye.cascade.rpc.managers.JsonDataManager;
import cn.skyeye.cascade.rpc.register.NodeRegister;
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

    private volatile static CascadeContext cascadeContext;

    private NodeManeger nodeManeger;
    private CascadeConf cascadeConf;
    private RpcContext rpcContext;
    private NodeRegister nodeRegister;
    private NettyTransferService transferService;

    private CascadeContext(){
        this.cascadeConf = new CascadeConf();
        this.rpcContext = RpcContext.get();
        this.nodeManeger = new NodeManeger(this);

        initTransferService();
        this.nodeRegister = new NodeRegister(this);
    }

    private void initTransferService(){
        NodeInfoDetail localNodeInfo = nodeManeger.getLocalNodeInfo();
        this.transferService = (NettyTransferService) rpcContext.newTransferService("skyeye",
                        localNodeInfo.getIp(),
                        cascadeConf.getPort(),
                        new FileDataManager(),
                        new JsonDataManager(this));
    }

    public static CascadeContext get() {
       if(cascadeContext == null){
           synchronized (CascadeContext.class){
               if(cascadeContext == null){
                   cascadeContext = new CascadeContext();
               }
           }
       }
       return cascadeContext;
    }

    public NodeManeger getNodeManeger() {
        return nodeManeger;
    }

    public CascadeConf getCascadeConf() {
        return cascadeConf;
    }

    public NodeRegister getNodeRegister() {
        return nodeRegister;
    }

    public NettyTransferService getTransferService() {
        return transferService;
    }

    public static void main(String[] args) throws Exception {
        /*
        CascadeContext cascadeContext = CascadeContext.get();
        */

        CascadeContext cascadeContext = CascadeContext.get();
        NodeRegister nodeRegister = cascadeContext.getNodeRegister();
        nodeRegister.registSubordinate("172.24.66.212", cascadeContext.nodeManeger.getLocalNodeInfo());

        Thread.sleep(100000000000L);
    }
}
