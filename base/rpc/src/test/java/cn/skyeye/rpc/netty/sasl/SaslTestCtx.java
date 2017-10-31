package cn.skyeye.rpc.netty.sasl;

import cn.skyeye.rpc.netty.TransportContext;
import cn.skyeye.rpc.netty.client.TransportClient;
import cn.skyeye.rpc.netty.client.TransportClientBootstrap;
import cn.skyeye.rpc.netty.server.RpcHandler;
import cn.skyeye.rpc.netty.server.TransportServer;
import cn.skyeye.rpc.netty.util.MapConfigProvider;
import cn.skyeye.rpc.netty.util.TransportConf;
import com.google.common.collect.ImmutableMap;

import java.net.InetAddress;
import java.util.*;

public class SaslTestCtx {

    final TransportClient client;
    final TransportServer server;

    private final boolean encrypt;
    private final boolean disableClientEncryption;
    private final EncryptionCheckerBootstrap checker;

    public SaslTestCtx(
        RpcHandler rpcHandler,
        boolean encrypt,
        boolean disableClientEncryption)
      throws Exception {

      this(rpcHandler, encrypt, disableClientEncryption, Collections.emptyMap());
    }

    SaslTestCtx(
        RpcHandler rpcHandler,
        boolean encrypt,
        boolean disableClientEncryption,
        Map<String, String> extraConf)
      throws Exception {

      Map<String, String> testConf = ImmutableMap.<String, String>builder()
        .putAll(extraConf)
        .put("rpc.authenticate.enableSaslEncryption", String.valueOf(encrypt))
        .build();
      TransportConf conf = new TransportConf("demo", new MapConfigProvider(testConf));

      SecretKeyHolder keyHolder = new SecretKeyHolder() {
          @Override
          public String getSaslUser(String appId) {
              return "jechedo";
          }

          @Override
          public String getSecretKey(String appId) {
              return "secret";
          }
      };

      TransportContext ctx = new TransportContext(conf, rpcHandler);

      this.checker = new EncryptionCheckerBootstrap(SaslEncryption.ENCRYPTION_HANDLER_NAME);

      this.server = ctx.createServer(Arrays.asList(new SaslServerBootstrap(conf, keyHolder),
        checker));

      try {
        List<TransportClientBootstrap> clientBootstraps = new ArrayList<>();
        clientBootstraps.add(new SaslClientBootstrap(conf, "user", keyHolder));
        if (disableClientEncryption) {
          clientBootstraps.add(new EncryptionDisablerBootstrap());
        }

        this.client = ctx.createClientFactory(clientBootstraps)
          .createClient(InetAddress.getLocalHost().getHostAddress(), server.getPort());
      } catch (Exception e) {
        close();
        throw e;
      }

      this.encrypt = encrypt;
      this.disableClientEncryption = disableClientEncryption;
    }

    void close() {
      if (!disableClientEncryption) {
          System.out.println(encrypt + " *** " + checker.foundEncryptionHandler);
      }
      if (client != null) {
        client.close();
      }
      if (server != null) {
        server.close();
      }
    }

}