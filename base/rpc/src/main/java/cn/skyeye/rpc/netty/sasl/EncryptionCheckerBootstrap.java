package cn.skyeye.rpc.netty.sasl;

import cn.skyeye.rpc.netty.server.RpcHandler;
import cn.skyeye.rpc.netty.server.TransportServerBootstrap;
import io.netty.channel.*;

@ChannelHandler.Sharable
public class EncryptionCheckerBootstrap extends ChannelOutboundHandlerAdapter
    implements TransportServerBootstrap {

    boolean foundEncryptionHandler;
    String encryptHandlerName;

    public EncryptionCheckerBootstrap(String encryptHandlerName) {
      this.encryptHandlerName = encryptHandlerName;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
      throws Exception {
      if (!foundEncryptionHandler) {
        foundEncryptionHandler =
          ctx.channel().pipeline().get(encryptHandlerName) != null;
      }
      ctx.write(msg, promise);
    }

    @Override
    public RpcHandler doBootstrap(Channel channel, RpcHandler rpcHandler) {
        channel.pipeline().addFirst("encryptionChecker", this);
        return rpcHandler;
    }

  }