package cn.skyeye.cascade.nodes;

import cn.skyeye.cascade.CascadeContext;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

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
