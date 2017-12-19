package cn.skyeye.cascade.rpc;

import cn.skyeye.cascade.CascadeContext;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/18 18:54
 */
public enum NodeStatus {
    success{
        @Override
        public String status() {
            return "连接成功";
        }
    },
    slow{
        @Override
        public String status() {
            return "连接异常";
        }
    },
    closed{
        @Override
        public String status() {
            return "连接断开";
        }
    };

    public abstract String status();

    /**
     * @param msGap   获取状态的时间 和 最后一次收到回应的时间间隔 单位为毫秒
     * @return
     */
    public static NodeStatus getNodeStatus(long msGap){
        long interval = CascadeContext.get().getHeartbeatManager().getHeartbeatSecondInterval() * 1000L;
        if(msGap <= interval * 2){
            return success;
        }else if(interval * 2 < msGap && msGap <= interval * 3){
            return slow;
        }else {
            return closed;
        }
    }
}
