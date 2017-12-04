package cn.skyeye.norths.services.syslog;

import cn.skyeye.common.json.Jsons;
import cn.skyeye.norths.events.DataEvent;
import cn.skyeye.norths.events.DataEventHandler;
import cn.skyeye.norths.utils.AlarmLogFilter;
import com.google.common.collect.Sets;
import org.productivity.java.syslog4j.SyslogIF;
import org.productivity.java.syslog4j.impl.net.tcp.TCPNetSyslog;
import org.productivity.java.syslog4j.impl.net.tcp.TCPNetSyslogConfig;
import org.productivity.java.syslog4j.impl.net.udp.UDPNetSyslog;
import org.productivity.java.syslog4j.impl.net.udp.UDPNetSyslogConfig;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/20 19:45
 */
public class Sysloger extends DataEventHandler {
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private Set<SyslogIF> syslogClients;
    private ReentrantLock lock = new ReentrantLock();

    private AlarmLogFilter alarmLogFilter;
    private SyslogConf syslogConf;

    private AtomicLong sendCount = new AtomicLong(0);

    public Sysloger(String name){
        super(name);
        this.syslogClients = Sets.newHashSet();
        this.syslogConf = new SyslogConf(conf_preffix, configDetail, northContext.getNorthsConf());
        initSyslogClient(syslogConf.getSyslogConfig());
        initAlarmFilter(syslogConf.getSyslogAlarmConfig());
    }

    public void initSyslogClient(SyslogConf.SyslogConfig syslogConf){
        lock.lock();
        try {
            if(!syslogConf.isOpen()){
                clearSyslogClient();
            }else {
                initSyslogClient(syslogConf.getProtocol(), syslogConf.getServices(), true);
            }
            if(syslogClients.isEmpty())
                logger.debug("无可用syslog服务器。");
        } finally {
            lock.unlock();
        }
    }

    private void initSyslogClient(String protocol, List<Map<String, Object>> services, boolean clear){
        if(clear)clearSyslogClient();
        Object ipObj;
        Object portObj;
        for(Map<String, Object> service : services){
            ipObj = service.get("host");
            if(ipObj == null)continue;

            portObj = service.get("port");
            if(portObj == null)continue;

            addSyslogClient(String.valueOf(ipObj),
                    Integer.parseInt(String.valueOf(portObj)),
                    protocol);
            logger.info(String.format("添加syslogClient成功: host: %s, port: %s, protocol : %s", ipObj, portObj, protocol));
        }
    }

    public void initAlarmFilter(SyslogConf.SyslogAlarmConfig syslogAlarmConfig){
        lock.lock();
        try {
            alarmLogFilter = new AlarmLogFilter(syslogAlarmConfig);
        } finally {
            lock.unlock();
        }
    }

    private void addSyslogClient(String host, int port, String protocol){
        SyslogIF client = null;
        switch (protocol.toLowerCase()){
            case "tcp":
                client = newTCPSyslogClient(host, port);
                break;
            case "udp":
                client = newUDPSyslogClient(host, port);
                break;
        }
        if(client != null) {
            this.syslogClients.add(client);
        }else {
            logger.error(String.format("不识别的协议类型：%s, host = %s, port = %s", protocol, host, port));
        }
    }

    private SyslogIF newUDPSyslogClient(String host, int port){
        UDPNetSyslog client = new UDPNetSyslog();
        UDPNetSyslogConfig syslogConfig = new UDPNetSyslogConfig();
        syslogConfig.setFacility("LOCAL3");
        syslogConfig.setSendLocalName(false);
        syslogConfig.setMaxMessageLength(10240);
        syslogConfig.setHost(host);
        syslogConfig.setPort(port);
        client.initialize("udp", syslogConfig);
        return client;
    }

    private SyslogIF newTCPSyslogClient(String host, int port){
        TCPNetSyslog client = new TCPNetSyslog();
        TCPNetSyslogConfig syslogConfig = new TCPNetSyslogConfig();
        syslogConfig.setFacility("LOCAL3");
        syslogConfig.setSendLocalName(false);
        syslogConfig.setMaxMessageLength(10240);
        syslogConfig.setHost(host);
        syslogConfig.setPort(port);
        client.initialize("tcp", syslogConfig);
        return client;
    }

    private void clearSyslogClient(){
        Iterator<SyslogIF> iterator = this.syslogClients.iterator();
        SyslogIF next;
        while (iterator.hasNext()){
            next = iterator.next();
            next.flush();
            next.shutdown();
            iterator.remove();
        }
        logger.info("清空syslogClient成功。");
    }


    @Override
    public void onEvent(DataEvent event) {
        this.lock.lock();
        Map<String, Object> record = event.getRecord();
        if(alarmLogFilter.isAccept(record)) {
            final String message = createMessage(record);
            syslogClients.forEach(entry -> {
                try {
                    entry.warn(message);
                } catch (Exception e) {
                    logger.error(String.format("syslog服务器：%s://%s:%s连接异常。",
                            entry.getProtocol(), entry.getConfig().getHost(),entry.getConfig().getPort()), e);
                }
            });
            logger.debug(String.format("发送告警日志的数目为：%s", sendCount.incrementAndGet()));
        }else{
            logger.debug(String.format("告警信息不满足要求：\n\t%s", event));
        }
        this.lock.unlock();
    }

    @Override
    public boolean isAccept(DataEvent event){
        return syslogConf.isAcceptSource(event.getSource(), event.getType());
    }

    private String createMessage(Map<String, Object> record){

        Set<String> includes = syslogConf.getIncludes();
        if(includes.size() > 0){
           record.keySet().forEach(field ->{
               if(!includes.contains(field)){
                   record.remove(field);
               }
           });
        }

        syslogConf.getExcludes().forEach(field -> record.remove(field));

        return String.format("alarm|!%s", Jsons.obj2JsonString(record));
    }

    public SyslogConf getSyslogConf() {
        return syslogConf;
    }
}
