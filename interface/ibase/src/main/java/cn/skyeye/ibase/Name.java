package cn.skyeye.ibase;

import cn.skyeye.common.net.HttpPoster;
import cn.skyeye.common.net.Https;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public  class Name{
    private String name;

    public Name(String name) {
        this.name = name;
    }

    public Name() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static void main(String[] args) throws Exception {
        HttpPoster post = Https.post("http://localhost:8088/skyeye/norths/config/syslog/edit", true);

        /*
        * {
   switch: “0” //syslog服务开关，0关闭，1开启
   protocol:”UDP”, // TCP 或者 UDP
   services:[
      {id:”1”,host:”127.0.0.1”,port:547},
      {id:”2”,host:”172.24.66.78”,port:547}
],
threat_switch: “1”, //警告日志发送开关， 0 关闭，1开启
systemlog_switch:”0”//系统日志发送开关， 0 关闭，1开启
  }

        * */

        List<Map<String, Object>> services = Lists.newArrayList();
        Map<String, Object> service1 = Maps.newHashMap();
        service1.put("id", "001");
        service1.put("host", "192.168.66.66");
        service1.put("port", 524);

        Map<String, Object> service2 = Maps.newHashMap();
        service2.put("id", "002");
        service2.put("host", "172.24.66.212");
        service2.put("port", 514);

        services.add(service1);
        services.add(service2);

        post.addParam("switch", "1");
        post.addParam("protocol", "UDP");
        post.addParam("threat_switch", "1");
        post.addParam("systemlog_switch", "0");
        post.addParam("services", services);

        String execute = post.execute("application/json");
        System.out.println(execute);

        System.out.println(19<<3);
    }
}