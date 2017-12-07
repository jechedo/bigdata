package cn.skyeye.rpc.zmq.demos;

import cn.skyeye.common.json.Jsons;
import com.google.common.collect.Maps;
import org.zeromq.ZMQ;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/4 18:40
 */
public class ZmqSlaver {

    public static void main(String[] args) {

        final AtomicInteger ids = new AtomicInteger(0);
        for (int j = 0;  j < 16; j++) {

            new Thread(new Runnable(){

                Map<String, Long> lastMasterRepTime = Maps.newHashMap();

                public void run() {

                    int id = ids.incrementAndGet();

                    // TODO Auto-generated method stub
                    ZMQ.Context context = ZMQ.context(1);  //创建一个I/O线程的上下文
                    ZMQ.Socket socket = context.socket(ZMQ.REQ);   //创建一个request类型的socket，这里可以将其简单的理解为客户端，用于向response端发送数据
                    socket.connect("tcp://127.0.0.1:5555");   //与response端建立连接
                    System.out.println("连接成功。");

                    Map<String, Object> message = Maps.newHashMap();
                    message.put("ip", "127.0.0.1");
                    message.put("id", Thread.currentThread().getName());
                    message.put("adderss", String.format("湖北襄阳-%s", id));
                    message.put("provence", "湖北");
                    message.put("department", String.format("it事业部-%s", id));
                    message.put("timestamp", System.currentTimeMillis());
                    message.put("type", "register");

                    socket.send(Jsons.obj2JsonString(message));   //向reponse端发送数据
                    String response = socket.recvStr();
                    checkMasterIsHealth(response);
                    System.out.println(String.format("master信息如下：\n\t %s", response));

                    for(int i = 0; i < 1000; i++){
                        message = Maps.newHashMap();
                        message.put("id", Thread.currentThread().getName());
                        message.put("type", "heartbeats");
                        message.put("timestamp", System.currentTimeMillis());
                        System.out.println(Thread.currentThread().getName() + "发送心跳");
                        socket.send(Jsons.obj2JsonString(message));

                        checkMasterIsHealth(socket.recvStr());
                    }
                }

                private void checkMasterIsHealth(String response){
                    try {
                        Map<String, Object> resMap = Jsons.toMap(response);
                        Object id = resMap.get("id");
                        Object time = resMap.get("timestamp");
                        if(id != null && time != null){
                            long newLastTime = (Long) time;
                            Long lastTime = lastMasterRepTime.get(id);
                            if(lastTime != null && (newLastTime - lastTime) > 10000){
                                System.err.println(Thread.currentThread().getName() + "：master连接不正常。");
                            }
                            lastMasterRepTime.put(String.valueOf(id), newLastTime);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }).start();
        }
    }
}
