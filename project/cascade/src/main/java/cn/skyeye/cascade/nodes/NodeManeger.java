package cn.skyeye.cascade.nodes;

import cn.skyeye.cascade.CascadeConf;
import cn.skyeye.cascade.CascadeContext;
import cn.skyeye.common.databases.DBCommon;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/12 18:35
 */
public class NodeManeger {

    private final Logger logger = Logger.getLogger(NodeManeger.class);

    private CascadeContext context;
    private NodeInfoDetail local;
    private Map<String, NodeInfoDetail> supNodeMap;
    private Map<String, NodeInfoDetail> subNodeMap;

    public NodeManeger(CascadeContext context){
        this.context = context;
        this.supNodeMap = Maps.newConcurrentMap();
        this.subNodeMap = Maps.newConcurrentMap();
        initNodes();
        Preconditions.checkNotNull(local, "本系统级联信息为空！");
    }

    private void setNodeInfoDetail(NodeInfoDetail nodeInfoDetail){
        int status = nodeInfoDetail.getStatus();
        switch (status){
            case 0 :
                this.local = nodeInfoDetail;
                logger.info(String.format("本系统级联配置：\n\t %s", nodeInfoDetail));
                break;
            case 1 :
                //nodeInfoDetail.setNodeLevel(NodeInfoDetail.NodeLevel.supervisor);
                this.supNodeMap.put(nodeInfoDetail.getId(), nodeInfoDetail);
                logger.info(String.format("本系统级联上级：\n\t %s", nodeInfoDetail));
                break;
            case 2 :
                //nodeInfoDetail.setNodeLevel(NodeInfoDetail.NodeLevel.subordinate);
                this.subNodeMap.put(nodeInfoDetail.getId(), nodeInfoDetail);
                logger.info(String.format("本系统级联下级：\n\t %s", nodeInfoDetail));
                break;
            default:
                throw new IllegalArgumentException(String.format("系统状态：%s不合法。\n\t %s", status, nodeInfoDetail));
        }

    }

    private void initNodes(){
        //nodes的初始化
        CascadeConf cascadeConf = context.getCascadeConf();
        Connection conn = cascadeConf.getConn();
        if(conn != null){
            String sql = String.format("select * from %s", cascadeConf.getCascadeTableName());
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                statement = conn.createStatement();
                resultSet = statement.executeQuery(sql);
                NodeInfoDetail nodeInfoDetail;
                String value;
                while (resultSet.next()){
                    nodeInfoDetail = new NodeInfoDetail();

                    value = resultSet.getString("system_id");
                    if(value == null)continue;
                    nodeInfoDetail.setId(value);

                    value = resultSet.getString("system_ip");
                    if(value == null) continue;
                    nodeInfoDetail.setIp(value);

                    value = resultSet.getString("system_name");
                    if(value == null) continue;
                    nodeInfoDetail.setName(value);

                    value = resultSet.getString("system_province");
                    if(value == null) continue;
                    nodeInfoDetail.setProvince(value);

                    value = resultSet.getString("system_city");
                    if(value == null) continue;
                    nodeInfoDetail.setCity(value);

                    value = resultSet.getString("system_status");
                    if(value != null)nodeInfoDetail.setStatus(Integer.parseInt(value));

                    value = resultSet.getString("registration_status");
                    if(value != null)nodeInfoDetail.setRegistrationStatus(Integer.parseInt(value));

                    value = resultSet.getString("registration_time");
                    if(value != null)nodeInfoDetail.setRegistrationTime(Long.parseLong(value));

                    value = resultSet.getString("parent_system_id");
                    if(value != null)nodeInfoDetail.setParentId(value);

                    value = resultSet.getString("connect_status");
                    if(value != null)nodeInfoDetail.setConnectStatus(value);

                    setNodeInfoDetail(nodeInfoDetail);
                }
            } catch (SQLException e) {
                logger.error(String.format("查询表：%s失败。", cascadeConf.getCascadeTableName()), e);
            } finally {
                DBCommon.close(null, statement, resultSet);
            }
        }
    }

    public void addAndUpdateNode(String nodeId, NodeInfoDetail.NodeLevel level, Map<String, Object> detail){

        switch (level){
            case supervisor:
                break;
            case subordinate:
                break;
        }
    }

    public NodeInfoDetail getLocalNodeInfo() {
        return local;
    }

    public boolean hasSupNode(){
        return !supNodeMap.isEmpty();
    }

    public void setSupNode(NodeInfoDetail supNode){

    }

    public void setSupNode(Map<String, String> supNode){

    }

    public boolean hasSubNode(){
        return !subNodeMap.isEmpty();
    }

    public void addSubNode(NodeInfoDetail subNode){

    }


    public static NodeInfoDetail createNodeInfoDetail(Map<String, String> nodeInfo){
        if(nodeInfo != null && !nodeInfo.isEmpty()) {
            String value;
            NodeInfoDetail nodeInfoDetail = new NodeInfoDetail();

            value = nodeInfo.get("id");
            if (value == null) return null;
            nodeInfoDetail.setId(value);

            value = nodeInfo.get("ip");
            if (value == null) return null;
            nodeInfoDetail.setIp(value);

            value = nodeInfo.get("name");
            if (value == null) return null;
            nodeInfoDetail.setName(value);

            value = nodeInfo.get("province");
            if (value == null) return null;
            nodeInfoDetail.setProvince(value);

            value = nodeInfo.get("city");
            if (value == null) return null;
            nodeInfoDetail.setCity(value);

            value = nodeInfo.get("status");
            if (value == null) return null;
            nodeInfoDetail.setStatus(Integer.parseInt(value));

            value = nodeInfo.get("registrationStatus");
            if (value != null) nodeInfoDetail.setRegistrationStatus(Integer.parseInt(value));

            value = nodeInfo.get("registrationTime");
            if (value != null) nodeInfoDetail.setRegistrationTime(Long.parseLong(value));

            value = nodeInfo.get("parentId");
            if (value != null) nodeInfoDetail.setParentId(value);

            value = nodeInfo.get("connectStatus");
            if (value != null) nodeInfoDetail.setConnectStatus(value);

            return nodeInfoDetail;
        }
        return null;
    }

}
