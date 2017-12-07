package cn.skyeye.rpc.zmq.demos;

import cn.skyeye.common.json.Jsons;
import cn.skyeye.rpc.netty.RpcContext;
import cn.skyeye.rpc.netty.transfers.TransferService;
import cn.skyeye.rpc.netty.transfers.messages.JsonMessageManager;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/5 11:11
 */
public class NettyMaster {


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
        RpcContext rpcContext = RpcContext.get();
        TransferService demo = rpcContext.newTransferService("demo", "localhost", 60080, null, new MasterJsonMessageManager());
        sleep();
    }

    private static class MasterJsonMessageManager implements JsonMessageManager {

        private Map<String, Map<String, Object>> slavers = Maps.newConcurrentMap();
        private Map<String, Long> lastMasterRepTime = Maps.newConcurrentMap();

        public byte[] handleMessage(String jsonMessage) {
            try {
                Map<String, Object> map = Jsons.toMap(jsonMessage);
                Object type = map.get("type");
                if (type != null) {
                    Object id = map.get("id");
                    Object time = map.get("timestamp");
                    if (id != null && time != null) {
                        switch (String.valueOf(type)) {
                            case "register":
                                System.out.println("收到注册信息：" + map);
                                slavers.put(String.valueOf(id), map);
                                lastMasterRepTime.put(String.valueOf(id), (Long)time);
                                Map<String, Object> message = Maps.newHashMap();
                                message.put("id", "master-" + Thread.currentThread().getName());
                                message.put("type", "master");
                                message.put("timestamp", System.currentTimeMillis());
                                return Jsons.obj2JsonString(message).getBytes();
                            default:
                                System.out.println("收到心跳信息：" + map);
                                long newLastTime = (Long) time;
                                Long lastTime = lastMasterRepTime.get(id);
                                if (lastTime != null && (newLastTime - lastTime) > 10000) {
                                    System.err.println(Thread.currentThread().getName() + "：master连接不正常。");
                                }
                                lastMasterRepTime.put(String.valueOf(id), newLastTime);
                                message = Maps.newHashMap();
                                message.put("id", "master-" + Thread.currentThread().getName());
                                message.put("type", "he");
                                message.put("timestamp", System.currentTimeMillis());
                                return Jsons.obj2JsonString(message).getBytes();
                        }
                    }else {
                        System.out.println("请求信息有误...");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
