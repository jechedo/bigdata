package cn.skyeye.rpc.netty.util;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/31 13:24
 */
public class NodeInfo {
    private String hostname;
    private String ip;
    private int port;

    public NodeInfo(String hostname, String ip, int port) {
        this.hostname = hostname;
        this.ip = ip;
        this.port = port;
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
        if (!hostname.equals(nodeInfo.hostname)) return false;
        return hostname.equals(nodeInfo.hostname);
    }

    @Override
    public int hashCode() {
        int result = hostname.hashCode();
        result = 31 * result + hostname.hashCode();
        result = 31 * result + port;
        return result;
    }
}
