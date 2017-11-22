package cn.skyeye.norths.services.syslog;

import cn.skyeye.common.json.Jsons;
import cn.skyeye.norths.events.DataEvent;
import cn.skyeye.norths.events.DataEventHandler;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogIF;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/20 19:45
 */
public class Sysloger extends DataEventHandler {
    public final static String NAME = "syslog";

    private final Logger logger = Logger.getLogger(Sysloger.class);

    private Map<String, SyslogIF> syslogClients;
    private ReentrantLock lock = new ReentrantLock();
    private AtomicLong totalEvent = new AtomicLong(0);
    private AtomicBoolean endOfBatch = new AtomicBoolean(false);

    private SyslogConf syslogConf;

    public Sysloger(){
        this.syslogClients = Maps.newConcurrentMap();
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

    private Map<String, SyslogIF> getSyslogClients(){
        this.lock.lock();
        Map<String, SyslogIF> map;
        try {
            map = Maps.newHashMap(syslogClients);
        } finally {
            this.lock.unlock();
        }
        return map;
    }

    @Override
    public void onEvent(DataEvent event, boolean endOfBatch) {
        if(syslogConf.isAcceeptSource(event.getSource())) {
            sendLog(event.getRecord());
            this.endOfBatch.set(endOfBatch);
        }
        totalEvent.incrementAndGet();
    }

    @Override
    public void shutdown(long total) {

        while (total > totalEvent.get()){
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {}
        }

        logger.info(String.format("数据总量为：%s, 处理数据量为：%s。", total, totalEvent.get()));
    }

    private void sendLog(Map<String, Object> record){
        final String message = createMessage(record);
        Set<Map.Entry<String, SyslogIF>> entries = getSyslogClients().entrySet();
        entries.forEach(entry -> {
            try {
                entry.getValue().warn(message);
            } catch (Exception e) {
                logger.error(String.format("syslog服务器：%s连接异常。", entry.getValue().getConfig().getHost()), e);
            }
        });
    }

    private String createMessage(Map<String, Object> record){

        syslogConf.getExcludes().forEach(field ->{
            record.remove(field);
        });

        Set<String> includes = syslogConf.getIncludes();
        if(includes.size() > 0){
           record.keySet().forEach(field ->{
               if(!includes.contains(field)){
                   record.remove(field);
               }
           });
        }

        return Jsons.obj2JsonString(record);
    }

    public boolean isEmpty(){
        return this.syslogClients.isEmpty();
    }
}
