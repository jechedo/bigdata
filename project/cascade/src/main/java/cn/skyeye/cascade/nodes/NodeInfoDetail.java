package cn.skyeye.cascade.nodes;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

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
    private AtomicLong lastConnectTime = new AtomicLong(0);

    //private NodeLevel nodeLevel;

    NodeInfoDetail(){}

    public NodeInfoDetail(NodeInfoDetail nodeInfoDetail){
        this.id = nodeInfoDetail.id;
        this.parentId = nodeInfoDetail.parentId;
        this.ip = nodeInfoDetail.ip;
        this.hostname = nodeInfoDetail.hostname;
        this.name = nodeInfoDetail.name;
        this.province = nodeInfoDetail.province;
        this.city = nodeInfoDetail.city;
        this.status = nodeInfoDetail.status;
        this.registrationStatus = nodeInfoDetail.registrationStatus;
        this.connectStatus = nodeInfoDetail.connectStatus;
        this.registrationTime = nodeInfoDetail.registrationTime;
        this.lastConnectTime = new AtomicLong(nodeInfoDetail.lastConnectTime.get());
    }

    public Map<String, String> getRegistMSG(String status){
        Map<String, String> map = Maps.newHashMap();
        map.put("id", id);
        map.put("status", status);
        map.put("ip", ip);
        map.put("parentId", parentId);
        map.put("name", name);
        map.put("province", province);
        map.put("city", city);
        return map;
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
        return lastConnectTime.get();
    }

    public void setLastConnectTime(long lastConnectTime) {
        this.lastConnectTime.set(lastConnectTime);
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
        sb.append(", lastConnectTime=").append(lastConnectTime.get());
        sb.append('}');
        return sb.toString();
    }
}
