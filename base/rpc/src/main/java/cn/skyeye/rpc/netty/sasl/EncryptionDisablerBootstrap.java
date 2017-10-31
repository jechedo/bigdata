package cn.skyeye.rpc.netty.sasl;

import cn.skyeye.rpc.netty.client.TransportClient;
import cn.skyeye.rpc.netty.client.TransportClientBootstrap;
import io.netty.channel.Channel;

public class EncryptionDisablerBootstrap implements TransportClientBootstrap {

    @Override
    public void doBootstrap(TransportClient client, Channel channel) {
      channel.pipeline().remove(SaslEncryption.ENCRYPTION_HANDLER_NAME);
    }

  }