package cn.skyeye.cascade.rpc.heartbeats;

import cn.skyeye.cascade.CascadeContext;
import cn.skyeye.cascade.nodes.NodeInfoDetail;
import cn.skyeye.cascade.rpc.NodeStatus;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.quartz.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/14 16:13
 */
public class HeartbeatManager {
    private final Logger logger = Logger.getLogger(HeartbeatManager.class);

    private HeartbeatSender sender;
    //心跳周期
    private int heartbeatSecondInterval = 5;
    private Map<String, Long> supsLastRespones = Maps.newConcurrentMap();

    private HeartbeatReceiver receiver;
    private Map<String, Long> subsLastRequest = Maps.newConcurrentMap();

    private CascadeContext cascadeContext;

    public HeartbeatManager(CascadeContext cascadeContext){
        this.cascadeContext = cascadeContext;
        Map<String, NodeInfoDetail> supNodeMap = cascadeContext.getNodeManeger().getSupNodeMap();
        supNodeMap.forEach((id, nodeInfo) ->{
            try {
                startHeartbeatSender(id, nodeInfo.getIp());
            } catch (SchedulerException e) {
                logger.error(String.format("启动心跳上传失败，上级为：\n\t %s", nodeInfo), e);
            }
        });

        this.receiver = new HeartbeatReceiver(cascadeContext);
    }

    public void startHeartbeatSender(String id,  String targetIp) throws SchedulerException {
      startHeartbeatSender(id, targetIp, cascadeContext.getCascadeConf().getPort());
    }

    public void startHeartbeatSender(String id,  String targetIp, int port) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(HeartbeatSender.class)
                .usingJobData("targetIp", targetIp)
                .usingJobData("targetPort", port)
                .usingJobData("targetId", id)
                .withIdentity(id, "skyeye-heartbeats")
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(id, "skyeye-heartbeats-triggers")
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(heartbeatSecondInterval))
                .startAt(new Date(System.currentTimeMillis() + heartbeatSecondInterval * 1000L))
                .build();

        cascadeContext.getJobManager().startJob(jobDetail, trigger);
    }

    public void shutdownHeartbeatSender(String id){
        cascadeContext.getJobManager().remove(new JobKey(id, "skyeye-heartbeats"));
    }

    void updateSupHeartbeatTime(String supId){
        NodeInfoDetail supNodeInfo = cascadeContext.getNodeManeger().getSupNodeInfo(supId);
        supNodeInfo.setLastConnectTime(System.currentTimeMillis());
    }

    void updateSubHeartbeatTime(String subId){
        NodeInfoDetail subNodeInfo = cascadeContext.getNodeManeger().getSubNodeInfo(subId);
        subNodeInfo.setLastConnectTime(System.currentTimeMillis());
    }

    public List<NodeInfoDetail> getSupNodeConnectStatus(){
        Map<String, NodeInfoDetail> supNodeMap = cascadeContext.getNodeManeger().getSupNodeMap();
        List<NodeInfoDetail> sups = Lists.newArrayListWithCapacity(supNodeMap.size());
        supNodeMap.forEach((id, info) ->{
            NodeInfoDetail nodeInfoDetail = getNodeInfoDetailWithConnectStatus(info);
            sups.add(nodeInfoDetail);
        });
        return sups;
    }

    public List<NodeInfoDetail> getSubNodeConnectStatus(){
        Map<String, NodeInfoDetail> subNodeMap = cascadeContext.getNodeManeger().getSubNodeMap();
        List<NodeInfoDetail> subs = Lists.newArrayListWithCapacity(subNodeMap.size());
        subNodeMap.forEach((id, info) ->{
            NodeInfoDetail nodeInfoDetail = getNodeInfoDetailWithConnectStatus(info);
            subs.add(nodeInfoDetail);
        });
        return subs;
    }

    private NodeInfoDetail getNodeInfoDetailWithConnectStatus(NodeInfoDetail info) {
        NodeInfoDetail nodeInfoDetail = new NodeInfoDetail(info);
        long lastConnectTime = nodeInfoDetail.getLastConnectTime();
        NodeStatus nodeStatus = NodeStatus.getNodeStatus(System.currentTimeMillis() - lastConnectTime);
        nodeInfoDetail.setConnectStatus(nodeStatus.status());
        return nodeInfoDetail;
    }


    public long getHeartbeatSecondInterval() {
        return heartbeatSecondInterval;
    }

    public HeartbeatReceiver getReceiver() {
        return receiver;
    }
}
