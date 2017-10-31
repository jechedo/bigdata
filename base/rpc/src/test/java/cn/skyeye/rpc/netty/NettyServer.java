package cn.skyeye.rpc.netty;

import cn.skyeye.rpc.netty.sasl.EncryptionCheckerBootstrap;
import cn.skyeye.rpc.netty.sasl.SaslServerBootstrap;
import cn.skyeye.rpc.netty.sasl.SecretKeyHolder;
import cn.skyeye.rpc.netty.server.TransportServer;
import cn.skyeye.rpc.netty.transfers.TransferService;
import cn.skyeye.rpc.netty.util.MapConfigProvider;
import cn.skyeye.rpc.netty.util.TransportConf;
import com.codahale.metrics.MetricSet;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/27 18:50
 */
public class NettyServer {

    public static void startServer(){
        HashMap<String, String> config = Maps.newHashMap();
        config.put("rpc.authenticate.enableSaslEncryption", String.valueOf(true));
        MapConfigProvider configProvider = new MapConfigProvider(config);
        TransportConf transportConf = new TransportConf("NIO", configProvider);

        SecretKeyHolder keyHolder = new SecretKeyHolder() {
            @Override
            public String getSaslUser(String appId) {
                return "server";
            }

            @Override
            public String getSecretKey(String appId) {
                return "secret";
            }
        };

        EncryptionCheckerBootstrap checker = new EncryptionCheckerBootstrap("saslEncryption");

        TransportContext transportContext = new TransportContext(transportConf, new NettyServerRpcHandler());
        TransportServer server = transportContext.createServer("localhost", 8811,
                Arrays.asList(new SaslServerBootstrap(transportConf, keyHolder),
                checker));

        MetricSet allMetrics = server.getAllMetrics();
        System.out.println(allMetrics.getMetrics());
        System.out.println(server.getPort());


        sleep();

    }

    private static void sleep() {
        try {
            while (true){
                Thread.sleep(1000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
         //startServer();
        RpcContext rpcContext = RpcContext.get();
        TransferService demo = rpcContext.newTransferService("demo", "172.24.66.212", 9911, null);
        sleep();
    }
}
