package cn.skyeye.rpc.netty;

import cn.skyeye.rpc.netty.client.TransportClientBootstrap;
import cn.skyeye.rpc.netty.client.TransportClientFactory;
import cn.skyeye.rpc.netty.sasl.EncryptionCheckerBootstrap;
import cn.skyeye.rpc.netty.sasl.SaslClientBootstrap;
import cn.skyeye.rpc.netty.sasl.SaslServerBootstrap;
import cn.skyeye.rpc.netty.server.RpcHandler;
import cn.skyeye.rpc.netty.server.TransportServer;
import cn.skyeye.rpc.netty.util.MapConfigProvider;
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

    public TransportClientFactory newClientFactory(String model,
                                                  RpcHandler rpcHandler,
                                                  Map<String, String> extraConf){
        if(extraConf == null)extraConf = Maps.newHashMap();
        extraConf.put("rpc.authenticate.enableSaslEncryption", String.valueOf(true));
        TransportConf transportConf = newTransportConf(model, extraConf);
        TransportContext transportContext = new TransportContext(transportConf, rpcHandler);

        return newTransportClientFactory(model,  transportContext);
    }

    public TransportClientFactory newTransportClientFactory(String model,
                                                            TransportContext transportContext) {
        List<TransportClientBootstrap> clientBootstraps = new ArrayList<>();
        clientBootstraps.add(new SaslClientBootstrap(transportContext.getConf(), model, rpcBaseConf.getKeyHolder()));
        return transportContext.createClientFactory(clientBootstraps);
    }

    public TransportServer newTransportServer(String model,
                                              RpcHandler rpcHandler,
                                              String host,
                                              int port,
                                              Map<String, String> extraConf){

        if(extraConf == null)extraConf = Maps.newHashMap();
        extraConf.put("rpc.authenticate.enableSaslEncryption", String.valueOf(true));
        TransportConf transportConf = newTransportConf(model, extraConf);
        TransportContext transportContext = newTransportContext(model, extraConf, rpcHandler);
        return newTransportServer(host, port, transportContext);
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

    public TransportContext newTransportContext(String model, Map<String, String> extraConf, RpcHandler rpcHandler){

        if(extraConf == null)extraConf = Maps.newHashMap();
        extraConf.put("rpc.authenticate.enableSaslEncryption", String.valueOf(true));
        TransportConf transportConf = newTransportConf(model, extraConf);
        return new TransportContext(transportConf, rpcHandler);
    }

    private TransportConf newTransportConf(String model, Map<String, String> conf){
        if(conf == null) conf = Maps.newHashMap();
        Map<String, String> config = rpcBaseConf.getConfigMapWithPrefix(String.format("rpc.%s.", model));
        config.putAll(conf);
        MapConfigProvider configProvider = new MapConfigProvider(config);
        return new TransportConf(model, configProvider);
    }

    public String getSystemId() {
        return rpcBaseConf.getSystemId();
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

    public static void main(String[] args) throws UnknownHostException {

    }

}
