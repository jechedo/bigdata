package cn.skyeye.cascade;

import cn.skyeye.cascade.nodes.NodeInfoDetail;
import cn.skyeye.cascade.nodes.NodeManeger;
import cn.skyeye.cascade.quartz.JobManager;
import cn.skyeye.cascade.rpc.heartbeats.HeartbeatManager;
import cn.skyeye.cascade.rpc.managers.FileDataManager;
import cn.skyeye.cascade.rpc.managers.JsonDataManager;
import cn.skyeye.cascade.rpc.register.NodeRegister;
import cn.skyeye.common.json.Jsons;
import cn.skyeye.rpc.netty.RpcContext;
import cn.skyeye.rpc.netty.client.RpcResponseCallback;
import cn.skyeye.rpc.netty.transfers.NettyTransferService;
import cn.skyeye.rpc.netty.util.JavaUtils;
import cn.skyeye.rpc.netty.util.NodeInfo;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/12 17:39
 */
public class CascadeContext {
    private final Logger logger = Logger.getLogger(CascadeContext.class);

    private volatile static CascadeContext cascadeContext;
    private String localID;
    private CascadeConf cascadeConf;

    private NodeManeger nodeManeger;
    private RpcContext rpcContext;
    private NodeRegister nodeRegister;
    private NettyTransferService transferService;
    private HeartbeatManager heartbeatManager;
    private JobManager jobManager;

    private ExecutorService threadPool;

    private CascadeContext(){
        this.cascadeConf = new CascadeConf();
        this.rpcContext = RpcContext.get();
        this.nodeManeger = new NodeManeger(this);
        this.localID = nodeManeger.getLocalNodeInfo().getId();

        try {
            this.jobManager = new JobManager(this);
        } catch (SchedulerException e) {
            logger.error("启动定时任务调度器失败。", e);
        }

        initTransferService();
        this.nodeRegister = new NodeRegister(this);
        this.heartbeatManager = new HeartbeatManager(this);

        this.threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

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

    public String getLocalID() {
        return localID;
    }

    public String sendJson(Object message, String targetIP, int timeOutMs) throws Exception{
        return sendJson(message, targetIP,  cascadeConf.getPort(), timeOutMs);
    }

    public String sendJson(Object message, String targetIP, int port, int timeOutMs) throws Exception{
        NodeInfo nodeInfo = new NodeInfo(localID, targetIP, port);
        final SettableFuture<ByteBuffer> result = SettableFuture.create();
        final List<Throwable> throwables = Lists.newArrayListWithCapacity(1);
        transferService.sendJson(nodeInfo, Jsons.obj2JsonString(message),  new RpcResponseCallback() {
            @Override
            public void onSuccess(ByteBuffer response) {
                ByteBuffer copy = ByteBuffer.allocate(response.remaining());
                copy.put(response);
                copy.flip();
                result.set(copy);
            }
            @Override
            public void onFailure(Throwable e) {
                throwables.add(e);
            }
        });

        //超时时间为5秒
        ByteBuffer byteBuffer = result.get(timeOutMs, TimeUnit.MILLISECONDS);
        if(!throwables.isEmpty())
            throw Throwables.propagate(throwables.get(0));

        return JavaUtils.bytesToString(byteBuffer);
    }

    public NettyTransferService getTransferService() {
        return transferService;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public HeartbeatManager getHeartbeatManager() {
        return heartbeatManager;
    }

    public static void main(String[] args) throws Exception {

        //CascadeContext cascadeContext = CascadeContext.get();

        CascadeContext cascadeContext = CascadeContext.get();
        NodeRegister nodeRegister = cascadeContext.getNodeRegister();
        nodeRegister.registSupervisor("172.24.66.212", cascadeContext.nodeManeger.getLocalNodeInfo());

        Thread.sleep(100000000000L);
    }
}
