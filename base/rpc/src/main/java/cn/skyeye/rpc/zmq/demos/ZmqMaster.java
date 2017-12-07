package cn.skyeye.rpc.zmq.demos;

import cn.skyeye.common.json.Jsons;
import com.google.common.collect.Maps;
import org.zeromq.ZMQ;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/4 18:40
 */
public class ZmqMaster {

    public static void main(String[] args) throws InterruptedException {

        Map<String, Map<String, Object>> slavers = Maps.newConcurrentMap();
        Map<String, Long> lastSlaversRepTime = Maps.newConcurrentMap();

        //这个表示创建用于一个I/O线程的context
        ZMQ.Context context = ZMQ.context(8);
        //创建一个response类型的socket，他可以接收request发送过来的请求，其实可以将其简单的理解为服务端
        ZMQ.Socket socket = context.socket(ZMQ.REP);
        socket.setSendTimeOut(-1);
        socket.setReceiveTimeOut(-1);
        //绑定端口
        socket.bind ("tcp://*:5555");

        String name = Thread.currentThread().getName();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        for(int i = 0; i < 1; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String request;
                    Map<String, Object> map;
                    Map<String, Object> message;
                    while (!Thread.currentThread().isInterrupted()) {
                        //获取request发送过来的数据
                        request = socket.recvStr();
                        System.out.println("request = " + request);

                        //String s = socket.recvStr();
                       // System.out.println("s = " + s);

                        message = Maps.newHashMap();
                        message.put("id", "master-" + name);
                        message.put("timestamp", System.currentTimeMillis());

                        try {
                            map = Jsons.toMap(request);
                            Object type = map.get("type");
                            if(type != null){
                                switch (String.valueOf(type)){
                                    case "register":
                                        System.out.println("收到注册：" + map);
                                        Object id = map.get("id");
                                        Object time = map.get("timestamp");
                                        if(id != null && time != null){
                                            slavers.put(String.valueOf(id), map);
                                            lastSlaversRepTime.put(String.valueOf(id), (Long)time);
                                            socket.send(Jsons.obj2JsonString(message));
                                        }else{
                                            socket.send("error");
                                        }
                                        break;
                                    case "heartbeats" :
                                        id = map.get("id");
                                        time = map.get("timestamp");
                                        System.out.println("收到心跳：" + map);
                                        if(id != null && time != null){
                                            long newLastTime = (Long) time;
                                            Long lastTime = lastSlaversRepTime.get(id);
                                            if(lastTime != null && (newLastTime - lastTime) > 10000){
                                                System.err.println(String.format("%s连接不正常。", slavers.get(id)));
                                            }
                                            lastSlaversRepTime.put(String.valueOf(id), newLastTime);
                                        }
                                        //Thread.sleep(1000 * 30);
                                        socket.send(Jsons.obj2JsonString(message));
                                        break;
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    countDownLatch.countDown();
                }
            }).start();
        }

        countDownLatch.await();
        socket.close();  //先关闭socket
        context.term();  //关闭当前的上下文
    }
}
