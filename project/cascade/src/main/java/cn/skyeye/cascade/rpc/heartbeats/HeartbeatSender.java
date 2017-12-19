package cn.skyeye.cascade.rpc.heartbeats;

import cn.skyeye.cascade.CascadeContext;
import cn.skyeye.cascade.nodes.NodeInfoDetail;
import cn.skyeye.cascade.rpc.MessageType;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/14 16:13
 */
public class HeartbeatSender implements Job {
    private final Logger logger = Logger.getLogger(HeartbeatSender.class);
    private CascadeContext cascadeContext;

    public HeartbeatSender(){
        this.cascadeContext = CascadeContext.get();
    }

    private String sendHeartbeat(String targetIP) throws Exception {
        NodeInfoDetail localNodeInfo = cascadeContext.getNodeManeger().getLocalNodeInfo();
        Map<String, String> msg = localNodeInfo.getRegistMSG("2");
        msg.put("type", MessageType.heartbeats.name());
        //msg.put("timestamp", String.valueOf(System.currentTimeMillis()));
       return cascadeContext.sendJson(msg, targetIP, 5000);
    }

    @Override
    public void execute(JobExecutionContext context){
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String targetIp = jobDataMap.getString("targetIp");

        try {
            String res = sendHeartbeat(targetIp);
            if(res.contains("ok"))
                cascadeContext.getHeartbeatManager()
                        .updateSupHeartbeatTime(jobDataMap.getString("targetId"));
        } catch (Exception e) {
            logger.error(String.format("发送心跳失败，上级IP为：%s", targetIp), e);
        }
    }
}
