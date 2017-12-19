package cn.skyeye.cascade.nodes;

import java.util.Objects;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/19 15:11
 */
public class NodeNetAddress {
    private String ip;
    private String hostname;
    private int port;

    public NodeNetAddress(String ip, String hostname, int port) {
        this.ip = ip;
        this.hostname = hostname;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeNetAddress that = (NodeNetAddress) o;
        return port == that.port &&
                Objects.equals(ip, that.ip) &&
                Objects.equals(hostname, that.hostname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, hostname, port);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NodeNetAddress{");
        sb.append("ip='").append(ip).append('\'');
        sb.append(", hostname='").append(hostname).append('\'');
        sb.append(", port=").append(port);
        sb.append('}');
        return sb.toString();
    }
}
