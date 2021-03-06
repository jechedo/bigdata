package cn.skyeye.cascade.nodes;

import cn.skyeye.cascade.CascadeConf;
import cn.skyeye.cascade.CascadeContext;
import cn.skyeye.common.databases.DBCommon;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/12 18:35
 */
public class NodeManeger {

    private static final Logger logger = Logger.getLogger(NodeManeger.class);

    private CascadeContext context;
    private NodeInfoDetail local;
    private Map<String, NodeInfoDetail> supNodeMap;
    private Map<String, NodeInfoDetail> subNodeMap;

    public NodeManeger(CascadeContext context){
        this.context = context;
        this.supNodeMap = Maps.newConcurrentMap();
        this.subNodeMap = Maps.newConcurrentMap();
        initNodes();
        if(local == null){
            this.local = localInfo();
        }
       //Preconditions.checkNotNull(local, "本系统级联信息为空！");
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

                    //配置默认端口
                    nodeInfoDetail.setPort(cascadeConf.getPort());

                    setNodeInfoDetail(nodeInfoDetail);
                }
            } catch (SQLException e) {
                logger.error(String.format("查询表：%s失败。", cascadeConf.getCascadeTableName()), e);
            } finally {
                DBCommon.close(null, statement, resultSet);
            }
        }
    }

    public NodeInfoDetail getLocalNodeInfo() {
        return local;
    }

    public boolean hasSupNode(){
        return !supNodeMap.isEmpty();
    }

    public void setSupNode(NodeInfoDetail supNode){
        this.supNodeMap.put(supNode.getId(), supNode);
    }


    public Map<String, NodeInfoDetail> getSupNodeMap() {
        return supNodeMap;
    }

    public Map<String, NodeInfoDetail> getSubNodeMap() {
        return subNodeMap;
    }

    public NodeInfoDetail getSupNodeInfo(String supNodeId){
        return this.supNodeMap.get(supNodeId);
    }

    public NodeInfoDetail getSubNodeInfo(String subNodeId){
        return this.subNodeMap.get(subNodeId);
    }

    public boolean hasSubNode(){
        return !subNodeMap.isEmpty();
    }

    public void addSubNode(NodeInfoDetail subNode){
        this.subNodeMap.put(subNode.getId(), subNode);
    }

    private NodeInfoDetail localInfo(){

        NodeInfoDetail nodeInfoDetail = new NodeInfoDetail();
        nodeInfoDetail.setId("8781dd16-19b1-4312-975a-49f69e9e83e5");
        nodeInfoDetail.setStatus(0);
        nodeInfoDetail.setIp("172.24.66.212");
        nodeInfoDetail.setName("武汉银行-skyeye");
        nodeInfoDetail.setProvince("湖北");
        nodeInfoDetail.setCity("武汉");
        nodeInfoDetail.setPort(context.getCascadeConf().getPort());
        nodeInfoDetail.setHostname("local");

        CascadeConf cascadeConf = context.getCascadeConf();
        Connection conn = cascadeConf.getConn();
        if(conn != null){
            String sql = String.format("insert into %s (system_id, system_ip, system_name, system_province, system_city, system_status) values (?,?,?,?,?,?)", cascadeConf.getCascadeTableName());
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, nodeInfoDetail.getId());
                preparedStatement.setString(2, nodeInfoDetail.getIp());
                preparedStatement.setString(3, nodeInfoDetail.getName());
                preparedStatement.setString(4, nodeInfoDetail.getProvince());
                preparedStatement.setString(5, nodeInfoDetail.getCity());
                preparedStatement.setInt(6, nodeInfoDetail.getStatus());

                preparedStatement.execute();
            } catch (SQLException e) {
                logger.info("写入数据到数据中失败。", e);
            }finally {
                DBCommon.close(preparedStatement);
            }
        }

        return nodeInfoDetail;
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

            value = nodeInfo.get("port");
            if (value != null){
                try {
                    nodeInfoDetail.setPort(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    nodeInfoDetail.setPort(CascadeContext.get().getCascadeConf().getPort());
                    logger.error(String.format("端口信息错误，使用默认端口。\n\t %s", nodeInfo), e);
                }
            }else {
                nodeInfoDetail.setPort(CascadeContext.get().getCascadeConf().getPort());
            }

            return nodeInfoDetail;
        }
        return null;
    }

}
