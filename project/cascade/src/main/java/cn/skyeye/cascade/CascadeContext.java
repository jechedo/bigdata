package cn.skyeye.cascade;

import cn.skyeye.cascade.nodes.NodeManeger;
import cn.skyeye.rpc.netty.RpcContext;
import cn.skyeye.rpc.netty.transfers.TransferService;
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


    private CascadeContext(){
        this.cascadeConf = new CascadeConf();
        //startServer();
        RpcContext rpcContext = RpcContext.get();
        TransferService demo = rpcContext.newTransferService("demo", "172.24.66.212", 60080, null, null);
    }


    public NodeManeger getNodeManeger() {
        return nodeManeger;
    }

    public CascadeConf getCascadeConf() {
        return cascadeConf;
    }
}
