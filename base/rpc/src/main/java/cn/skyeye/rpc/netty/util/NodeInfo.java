package cn.skyeye.rpc.netty.util;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/31 13:24
 */
public class NodeInfo {
    private String systemId;
    private String hostname;
    private String ip;
    private int port;

    public NodeInfo(String systemId, String hostname, String ip, int port) {
        this.systemId = systemId;
        this.hostname = hostname;
        this.ip = ip;
        this.port = port;
    }

    public String getSystemId() {
        return systemId;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NodeInfo{");
        sb.append("systemId='").append(systemId).append('\'');
        sb.append(", hostname='").append(hostname).append('\'');
        sb.append(", ip='").append(ip).append('\'');
        sb.append(", port=").append(port);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeInfo nodeInfo = (NodeInfo) o;

        if (port != nodeInfo.port) return false;
        if (!systemId.equals(nodeInfo.systemId)) return false;
        if (!hostname.equals(nodeInfo.hostname)) return false;
        return hostname.equals(nodeInfo.hostname);
    }

    @Override
    public int hashCode() {
        int result = systemId.hashCode();
        result = 31 * result + hostname.hashCode();
        result = 31 * result + hostname.hashCode();
        result = 31 * result + port;
        return result;
    }
}
