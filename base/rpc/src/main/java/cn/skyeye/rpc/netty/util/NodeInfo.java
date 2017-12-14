package cn.skyeye.rpc.netty.util;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/31 13:24
 */
public class NodeInfo {
    private String localID;
    private String targetIP;
    private int targetPort;

    public NodeInfo(String localID, String targetIP, int targetPort) {
        this.localID = localID;
        this.targetIP = targetIP;
        this.targetPort = targetPort;
    }

    public String getLocalID() {
        return localID;
    }

    public String getTargetIP() {
        return targetIP;
    }

    public int getTargetPort() {
        return targetPort;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NodeInfo{");
        sb.append(", localID='").append(localID).append('\'');
        sb.append(", targetIP='").append(targetIP).append('\'');
        sb.append(", targetPort=").append(targetPort);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeInfo nodeInfo = (NodeInfo) o;

        if (targetPort != nodeInfo.targetPort) return false;
        if (!localID.equals(nodeInfo.localID)) return false;
        return localID.equals(nodeInfo.localID);
    }

    @Override
    public int hashCode() {
        int result = localID.hashCode();
        result = 31 * result + localID.hashCode();
        result = 31 * result + targetPort;
        return result;
    }
}
