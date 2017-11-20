package cn.skyeye.norths.syslog;

import cn.skyeye.norths.sources.DataSource;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogIF;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/20 19:45
 */
public class Sysloger {

    private final Logger logger = Logger.getLogger(Sysloger.class);

    private Map<String, SyslogIF> syslogClients;
    private ReentrantLock lock = new ReentrantLock();

    private DataSource dataSource;
    private Timer timer;

    public Sysloger(DataSource dataSource){
        this.syslogClients = Maps.newHashMap();
        this.dataSource = dataSource;
    }

    public void addSyslogClient(String id, String host, int port, String protocol){
        this.lock.lock();
        try {
            SyslogIF syslogClient = newSyslogClient(host, port, protocol);
            this.syslogClients.put(id, syslogClient);
        } finally {
            this.lock.unlock();
        }
    }

    private SyslogIF newSyslogClient(String host, int port, String protocol) {
        SyslogIF syslogClient = Syslog.getInstance(protocol);
        syslogClient.getConfig().setHost(host);
        syslogClient.getConfig().setPort(port);
        return syslogClient;
    }

    public void deleteSyslogClient(String id){
        this.lock.lock();
        this.syslogClients.remove(id);
        this.lock.unlock();
    }

    public void editSyslogClient(String id, String host, int port, String protocol){
       addSyslogClient(id, host, port, protocol);
    }

    public Map<String, SyslogIF> getSyslogClients(){
        this.lock.lock();
        Map<String, SyslogIF> map;
        try {
            map = Maps.newHashMap(syslogClients);
        } finally {
            this.lock.unlock();
        }
        return map;
    }

    public void start(){
        timer = new Timer("sysloger");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                List<String> data = dataSource.readData();
                Set<Map.Entry<String, SyslogIF>> entries = getSyslogClients().entrySet();
                entries.forEach(entry ->{
                    data.forEach(entry.getValue()::warn);
                });
            }
        }, 0, 5 * 60 * 1000L);
    }
}
