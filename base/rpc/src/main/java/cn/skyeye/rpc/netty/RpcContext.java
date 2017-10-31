package cn.skyeye.rpc.netty;

import cn.skyeye.rpc.netty.client.TransportClient;
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/31 13:28
 */
public class RpcContext {
    private final Logger logger = Logger.getLogger(RpcContext.class);

    private static RpcContext rpcContext;

    private RpcBaseConf rpcBaseConf;
    private TransportClientFactory clientFactory;
    private AtomicBoolean clientFactoryInited;

    private TransportServer server;
    private AtomicBoolean serverInited;


    private RpcContext(boolean loadEnv){
        this.rpcBaseConf = new RpcBaseConf(loadEnv);
        this.clientFactoryInited = new AtomicBoolean(false);
        this.serverInited = new AtomicBoolean(false);
    }

    public synchronized void initClientFactory(String model,
                                               RpcHandler rpcHandler,
                                               Map<String, String> extraConf){
        if(!clientFactoryInited.get()){
            if(extraConf == null)extraConf = Maps.newHashMap();
            extraConf.put("rpc.authenticate.enableSaslEncryption", String.valueOf(true));
            TransportConf transportConf = newTransportConf(model, extraConf);

            List<TransportClientBootstrap> clientBootstraps = new ArrayList<>();
            clientBootstraps.add(new SaslClientBootstrap(transportConf, "skyeye", rpcBaseConf.getKeyHolder()));
            TransportContext transportContext = new TransportContext(transportConf, rpcHandler);
            this.clientFactory = transportContext.createClientFactory(clientBootstraps);

            clientFactoryInited.set(true);
        }
    }

    public synchronized TransportServer initServer(String model,
                                                    RpcHandler rpcHandler,
                                                    String host,
                                                    int port,
                                                    Map<String, String> extraConf){
        if(!serverInited.get()){
            if(extraConf == null)extraConf = Maps.newHashMap();
            extraConf.put("rpc.authenticate.enableSaslEncryption", String.valueOf(true));

            TransportConf transportConf = newTransportConf(model, extraConf);
            EncryptionCheckerBootstrap checker = new EncryptionCheckerBootstrap("saslEncryption");

            TransportContext transportContext = new TransportContext(transportConf, rpcHandler);
            if(host == null){
                this.server = transportContext.createServer(port,
                        Arrays.asList(new SaslServerBootstrap(transportConf, rpcBaseConf.getKeyHolder()),
                                checker));
            }else {
                this.server = transportContext.createServer(host, port,
                        Arrays.asList(new SaslServerBootstrap(transportConf, rpcBaseConf.getKeyHolder()),
                                checker));
            }
            serverInited.set(true);
        }

        return server;
    }

    private TransportConf newTransportConf(String model, Map<String, String> conf){
        if(conf == null) conf = Maps.newHashMap();
        Map<String, String> config = rpcBaseConf.getConfigMapWithPrefix(String.format("rpc.%s.", model));
        config.putAll(conf);
        MapConfigProvider configProvider = new MapConfigProvider(config);
        return new TransportConf(model, configProvider);
    }

    public TransportServer getRpcServer() {
        return server;
    }

    public TransportClient getRpcClient(String remoteHost, int remotePort) throws Exception {
        return clientFactory.createClient(remoteHost, remotePort);
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
