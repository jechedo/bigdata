package cn.skyeye.cascade.nodes;

import cn.skyeye.cascade.CascadeConf;
import cn.skyeye.cascade.CascadeContext;
import cn.skyeye.common.databases.DBCommon;
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

    private void setNodeInfoDetail(NodeInfoDetail nodeInfoDetail){
        int status = nodeInfoDetail.getStatus();
        switch (status){
            case 0 :
                this.local = nodeInfoDetail;
                logger.info(String.format("本系统级联配置：\n\t %s", nodeInfoDetail));
                break;
            case 1 :
                this.supNodeMap.put(nodeInfoDetail.getId(), nodeInfoDetail);
                logger.info(String.format("本系统级联上级：\n\t %s", nodeInfoDetail));
                break;
            case 2 :
                this.subNodeMap.put(nodeInfoDetail.getId(), nodeInfoDetail);
                logger.info(String.format("本系统级联下级：\n\t %s", nodeInfoDetail));
                break;
            default:
                throw new IllegalArgumentException(String.format("系统状态：%s不合法。\n\t %s", status, nodeInfoDetail));
        }

    }

    public void addAndUpdateNode(String nodeId, Map<String, Object> detail){

        Object level = detail.get("level");
        if(level == null)
            level = NodeInfoDetail.NodeLevel.subordinate.name();
        if("subordinate".equals(level)){
            //下级
        }else{
            //上级
        }
    }

    public boolean hasSupNode(){
        return !supNodeMap.isEmpty();
    }

    public boolean hasSubNode(){
        return !subNodeMap.isEmpty();
    }

}
