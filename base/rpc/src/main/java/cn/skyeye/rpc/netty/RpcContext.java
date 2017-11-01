package cn.skyeye.rpc.netty;

import cn.skyeye.rpc.netty.client.TransportClientBootstrap;
import cn.skyeye.rpc.netty.client.TransportClientFactory;
import cn.skyeye.rpc.netty.sasl.EncryptionCheckerBootstrap;
import cn.skyeye.rpc.netty.sasl.SaslClientBootstrap;
import cn.skyeye.rpc.netty.sasl.SaslServerBootstrap;
import cn.skyeye.rpc.netty.server.RpcHandler;
import cn.skyeye.rpc.netty.server.TransportServer;
import cn.skyeye.rpc.netty.transfers.NettyTransferService;
import cn.skyeye.rpc.netty.transfers.TransferService;
import cn.skyeye.rpc.netty.transfers.blocks.BlockDataManager;
import cn.skyeye.rpc.netty.transfers.messages.JsonMessageManager;
import cn.skyeye.rpc.netty.util.MapConfigProvider;
import cn.skyeye.rpc.netty.util.NodeInfo;
import cn.skyeye.rpc.netty.util.TransportConf;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/31 13:28
 */
public class RpcContext {
    public final static String APPID = "skyeye-rpc";
    private final Logger logger = Logger.getLogger(RpcContext.class);

    private static RpcContext rpcContext;

    private RpcBaseConf rpcBaseConf;

    private RpcContext(boolean loadEnv){
        this.rpcBaseConf = new RpcBaseConf(loadEnv);
    }

    public TransportClientFactory newTransportClientFactory(String model,
                                                            TransportContext transportContext) {
        List<TransportClientBootstrap> clientBootstraps = new ArrayList<>();
        clientBootstraps.add(new SaslClientBootstrap(transportContext.getConf(), model, rpcBaseConf.getKeyHolder()));
        return transportContext.createClientFactory(clientBootstraps);
    }

    public TransportServer newTransportServer(String host,
                                              int port,
                                              TransportContext transportContext) {
        EncryptionCheckerBootstrap checker = new EncryptionCheckerBootstrap("saslEncryption");
        TransportServer server;
        if(host == null){
           server = transportContext.createServer(port,
                    Arrays.asList(new SaslServerBootstrap(transportContext.getConf(), rpcBaseConf.getKeyHolder()),
                            checker));
        }else {
           server = transportContext.createServer(host, port,
                    Arrays.asList(new SaslServerBootstrap(transportContext.getConf(), rpcBaseConf.getKeyHolder()),
                            checker));
        }

        return server;
    }

    public TransportContext newTransportContext(TransportConf transportConf, RpcHandler rpcHandler){
        return new TransportContext(transportConf, rpcHandler);
    }

    public TransportConf newTransportConf(String model, Map<String, String> conf){
        if(conf == null)conf = Maps.newHashMap();
        conf.put("rpc.authenticate.enableSaslEncryption", String.valueOf(true));
        Map<String, String> config = rpcBaseConf.getConfigMapWithPrefix(String.format("rpc.%s.", model));
        config.putAll(conf);
        MapConfigProvider configProvider = new MapConfigProvider(config);
        return new TransportConf(model, configProvider);
    }

    public String getHostname() {
        return rpcBaseConf.getHostname();
    }

    public static RpcContext get(){
        return get(true);
    }

    public static RpcContext get(boolean loadEnv){
        if(rpcContext == null){
            synchronized (RpcContext.class){
                if(rpcContext == null){
                    rpcContext = new RpcContext(loadEnv);
                }
            }
        }
        return rpcContext;
    }

    public TransferService newTransferService(String appId,
                                              String ip,
                                              int port,
                                              BlockDataManager blockDataManager,
                                              JsonMessageManager jsonMessageManager){
        NodeInfo nodeInfo = new NodeInfo(getHostname(), ip, port);
        NettyTransferService nettyTransferService = new NettyTransferService(appId, nodeInfo);
        nettyTransferService.init(blockDataManager, jsonMessageManager);
        return nettyTransferService;
    }

    public static void main(String[] args) throws UnknownHostException {
    }
}
