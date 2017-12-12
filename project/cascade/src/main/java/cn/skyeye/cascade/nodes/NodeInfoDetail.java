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
    private String hostname;
    private String ip;
    //单位名称
    private String name;
    //单位省份
    private String province;
    //单位城市
    private String city;
    //最后一次连接时间
    private long lastConnectTime;

    private NodeLevel nodeLevel;


    NodeInfoDetail(Map<String, Object> heartbeats){

        Object id = heartbeats.get("id");


    }

}
