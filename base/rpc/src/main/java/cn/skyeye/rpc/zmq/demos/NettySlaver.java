package cn.skyeye.rpc.zmq.demos;

import cn.skyeye.common.json.Jsons;
import cn.skyeye.rpc.netty.RpcContext;
import cn.skyeye.rpc.netty.client.RpcResponseCallback;
import cn.skyeye.rpc.netty.transfers.NettyTransferService;
import cn.skyeye.rpc.netty.util.JavaUtils;
import cn.skyeye.rpc.netty.util.NodeInfo;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.SettableFuture;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/5 11:11
 */
public class NettySlaver {

    private static void sleep() {
        try {
            while (true){
                Thread.sleep(1000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        final AtomicInteger ids = new AtomicInteger(0);
        for (int j = 0;  j < 1; j++) {

            new Thread(()->{

                SlaverJsonMessageManager slaverJsonMessageManager = new SlaverJsonMessageManager();

                int id = ids.incrementAndGet();
                int port = 60080 + id;
                RpcContext rpcContext = RpcContext.get();
                NettyTransferService demo = (NettyTransferService)rpcContext.newTransferService("demo", "localhost", port, null,
                        null);

                NodeInfo master = new NodeInfo("localhost", "localhost", 60080);
                Map<String, Object> message = Maps.newHashMap();
                message.put("ip", "127.0.0.1");
                message.put("id", Thread.currentThread().getName());
                message.put("adderss", String.format("湖北襄阳-%s", id));
                message.put("provence", "湖北");
                message.put("department", String.format("it事业部-%s", id));
                message.put("timestamp", System.currentTimeMillis());
                message.put("type", "register");

                sendMessage(slaverJsonMessageManager, demo, master, message);
                for(int i = 0; i < 10; i++){
                    message = Maps.newHashMap();
                    message.put("id", Thread.currentThread().getName());
                    message.put("type", "heartbeats");
                    message.put("timestamp", System.currentTimeMillis());
                    System.out.println(Thread.currentThread().getName() + "发送心跳");
                    sendMessage(slaverJsonMessageManager, demo, master, message);
                }

            }).start();
        }
        sleep();
    }

    private static void sendMessage(SlaverJsonMessageManager slaverJsonMessageManager,
                                    NettyTransferService demo,
                                    NodeInfo master,
                                    Map<String, Object> message) {
        final SettableFuture<ByteBuffer> result = SettableFuture.create();
        demo.sendJson(master, Jsons.obj2JsonString(message), new RpcResponseCallback() {
            @Override
            public void onSuccess(ByteBuffer response) {
                ByteBuffer copy = ByteBuffer.allocate(response.remaining());
                copy.put(response);
                // flip "copy" to make it readable
                copy.flip();
                result.set(copy);
            }
            @Override
            public void onFailure(Throwable e) {
                result.setException(e);
            }
        });

        try {
            ByteBuffer byteBuffer = result.get(3000, TimeUnit.MILLISECONDS);
            String res = JavaUtils.bytesToString(byteBuffer);
            slaverJsonMessageManager.handleMessage(res);
        } catch (ExecutionException e) {
            throw Throwables.propagate(e.getCause());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static class SlaverJsonMessageManager{

        private Map<String, Object> master;
        private Map<String, Long> lastMasterRepTime = Maps.newHashMap();

        public void handleMessage(String jsonMessage) {
            try {
                Map<String, Object> map = Jsons.toMap(jsonMessage);
                Object type = map.get("type");
                if(type != null){
                    switch (String.valueOf(type)){
                        case "master":
                            this.master = map;
                            System.out.println("收到master信息：" + master);
                            break;
                        default:
                            System.out.println("收到心跳反馈信息：" + master);
                            Object id = map.get("id");
                            Object time = map.get("timestamp");
                            if(id != null && time != null){
                                long newLastTime = (Long) time;
                                Long lastTime = lastMasterRepTime.get(id);
                                if(lastTime != null && (newLastTime - lastTime) > 10000){
                                    System.err.println(Thread.currentThread().getName() + "：master连接不正常。");
                                }
                                lastMasterRepTime.put(String.valueOf(id), newLastTime);
                            }else{
                                System.out.println("返回信息有误...");
                            }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
