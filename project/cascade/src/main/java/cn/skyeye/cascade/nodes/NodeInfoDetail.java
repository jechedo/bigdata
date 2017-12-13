package cn.skyeye.cascade.nodes;

import java.util.Map;

/**
 * Description:
 *   上报信息节点的细节
 * @author LiXiaoCong
 * @version 2017/12/12 18:18
 */
public class NodeInfoDetail {

    public enum NodeLevel{supervisor, subordinate}

    //唯一标识
    private String id;
    private String parentId;
    private String ip;
    private String hostname;
    //单位名称
    private String name;
    //单位省份
    private String province;
    //单位城市
    private String city;

    private int status = 0;

    private int registrationStatus = -100;

    private String connectStatus;

    private long registrationTime;

    //最后一次连接时间
    private long lastConnectTime;

    private NodeLevel nodeLevel;

    public NodeInfoDetail(){}

    public NodeInfoDetail(Map<String, Object> heartbeats){

        Object id = heartbeats.get("id");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(int registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public String getConnectStatus() {
        return connectStatus;
    }

    public void setConnectStatus(String connectStatus) {
        this.connectStatus = connectStatus;
    }

    public long getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(long registrationTime) {
        this.registrationTime = registrationTime;
    }

    public long getLastConnectTime() {
        return lastConnectTime;
    }

    public void setLastConnectTime(long lastConnectTime) {
        this.lastConnectTime = lastConnectTime;
    }

    public NodeLevel getNodeLevel() {
        return nodeLevel;
    }

    public void setNodeLevel(NodeLevel nodeLevel) {
        this.nodeLevel = nodeLevel;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NodeInfoDetail{");
        sb.append("id='").append(id).append('\'');
        sb.append(", parentId='").append(parentId).append('\'');
        sb.append(", ip='").append(ip).append('\'');
        sb.append(", hostname='").append(hostname).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", province='").append(province).append('\'');
        sb.append(", city='").append(city).append('\'');
        sb.append(", status=").append(status);
        sb.append(", registrationStatus=").append(registrationStatus);
        sb.append(", connectStatus='").append(connectStatus).append('\'');
        sb.append(", registrationTime=").append(registrationTime);
        sb.append(", lastConnectTime=").append(lastConnectTime);
        sb.append('}');
        return sb.toString();
    }
}
