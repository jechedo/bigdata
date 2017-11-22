package cn.skyeye.common;

import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogIF;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/10 15:59
 */
public class Syslogs {


    public static void main(String[] args) {
        SyslogIF tcp = Syslog.getInstance("tcp");
        tcp.getConfig().setHost("192.168.66.66");
        tcp.getConfig().setPort(514);

        while (true) {
            String alarmJson = "hello world";
            tcp.warn(alarmJson);
            System.out.println("....");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
