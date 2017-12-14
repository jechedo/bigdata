package cn.skyeye.cascade.rpc.register;

import cn.skyeye.cascade.CascadeContext;
import cn.skyeye.cascade.nodes.NodeInfoDetail;
import cn.skyeye.cascade.rpc.MessageType;
import cn.skyeye.common.json.Jsons;
import cn.skyeye.rpc.netty.client.RpcResponseCallback;
import cn.skyeye.rpc.netty.transfers.NettyTransferService;
import cn.skyeye.rpc.netty.util.JavaUtils;
import cn.skyeye.rpc.netty.util.NodeInfo;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 *   注册相关
 * @author LiXiaoCong
 * @version 2017/12/14 9:52
 */
public class NodeRegister {
    private final Logger logger = Logger.getLogger(NodeRegister.class);

    protected CascadeContext cascadeContext;
    protected NettyTransferService transferService;
    public NodeRegister(CascadeContext cascadeContext){
        this.cascadeContext = cascadeContext;
        this.transferService = this.cascadeContext.getTransferService();
    }

    private String regist(String localID, Map<String, String> resistMSG, String targetIP) throws Exception{

        NodeInfo nodeInfo = new NodeInfo(localID, targetIP, cascadeContext.getCascadeConf().getPort());

        final SettableFuture<ByteBuffer> result = SettableFuture.create();
        final List<Throwable> throwables = Lists.newArrayListWithCapacity(1);
        transferService.sendJson(nodeInfo, Jsons.obj2JsonString(resistMSG),  new RpcResponseCallback() {
            @Override
            public void onSuccess(ByteBuffer response) {
                ByteBuffer copy = ByteBuffer.allocate(response.remaining());
                copy.put(response);
                copy.flip();
                result.set(copy);
            }
            @Override
            public void onFailure(Throwable e) {
                throwables.add(e);
            }
        });

        //超时时间为5秒
        ByteBuffer byteBuffer = result.get(5, TimeUnit.SECONDS);
        if(!throwables.isEmpty())
            throw Throwables.propagate(throwables.get(0));

        return JavaUtils.bytesToString(byteBuffer);
    }

    public void registSubordinate(String targetIP, NodeInfoDetail localInfo) throws Exception {
        Map<String, String> registMSG = localInfo.getRegistMSG("2");
        registMSG.put("type", MessageType.register.name());
        String reponse = regist(localInfo.getId(), registMSG, targetIP);
    }

    public void registSupervisor(String targetIP, NodeInfoDetail localInfo) throws Exception {
        Map<String, String> registMSG = localInfo.getRegistMSG("1");
        registMSG.put("type", MessageType.register.name());
        String reponse = regist(localInfo.getId(), registMSG, targetIP);
        System.err.println(reponse);


    }
}
