package cn.skyeye.norths.services.syslog;

import cn.skyeye.common.json.Jsons;
import cn.skyeye.norths.events.DataEvent;
import cn.skyeye.norths.events.DataEventHandler;
import cn.skyeye.norths.utils.AlarmLogFilter;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.productivity.java.syslog4j.SyslogIF;
import org.productivity.java.syslog4j.impl.net.tcp.TCPNetSyslog;
import org.productivity.java.syslog4j.impl.net.tcp.TCPNetSyslogConfig;
import org.productivity.java.syslog4j.impl.net.udp.UDPNetSyslog;
import org.productivity.java.syslog4j.impl.net.udp.UDPNetSyslogConfig;

import java.net.InetAddress;
import java.util.*;
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
    private Set<SyslogClientInfo> failedsyslogClients;
    private ReentrantLock lock = new ReentrantLock();
    private Timer failedClientCheckTimer;

    private AlarmLogFilter alarmLogFilter;
    private SyslogConf syslogConf;

    private AtomicLong sendCount = new AtomicLong(0);

    public Sysloger(String name){
        super(name);
        this.syslogClients =  Sets.newConcurrentHashSet();
        this.failedsyslogClients = Sets.newConcurrentHashSet();
        this.syslogConf = new SyslogConf(conf_preffix, configDetail, northContext.getNorthsConf());
        this.alarmLogFilter = new AlarmLogFilter();

        initAlarmFilter(syslogConf.getSyslogAlarmConfig());
        initSyslogClient(syslogConf.getSyslogConfig());

        this.failedClientCheckTimer = new Timer("failedClientCheckTimer");
        failedClientCheckTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Iterator<SyslogClientInfo> iterator = failedsyslogClients.iterator();
                SyslogClientInfo next;
                while (iterator.hasNext()){
                   next = iterator.next();
                   logger.info(String.format("重新检查syslog服务器ip：%s的连通性。", next.host));
                   if(isReachable(next.host)){
                       addSyslogClient(next.host, next.port, next.protocol);
                       iterator.remove();
                   }else {
                       logger.warn(String.format("重新检查syslog服务器ip：%s的连通性失败。", next.host));
                   }
                }
            }
        }, 300000L, 300000L);
    }

    public void initAlarmFilter(SyslogConf.SyslogAlarmConfig syslogAlarmConfig){
        this.alarmLogFilter.initConfig(syslogAlarmConfig);
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

    private Set<SyslogIF> getSyslogClients(){
        this.lock.lock();
        try {
            return Sets.newHashSet(syslogClients);
        } finally {
            this.lock.unlock();
        }
    }
    private void removeSyslogClients(SyslogIF syslogClient){
        this.lock.lock();
        try {
            syslogClients.remove(syslogClient);
        } finally {
            this.lock.unlock();
        }

    }

    private void initSyslogClient(String protocol, List<Map<String, Object>> services, boolean clear){
        if(clear)clearSyslogClient();
        Object ipObj;
        String ip;
        Object portObj;
        int port;
        for(Map<String, Object> service : services){

            portObj = service.get("port");
            if(portObj == null){
                logger.error(String.format("服务%s的port为空。", service));
                continue;
            }
            port = Integer.parseInt(String.valueOf(portObj));

            ipObj = service.get("host");
            if(ipObj == null){
                logger.error(String.format("服务%s的ip为空。", service));
                continue;
            }
            ip = String.valueOf(ipObj);

            if(!isReachable(ip)){
                failedsyslogClients.add(new SyslogClientInfo(ip, port, protocol));
                logger.error(String.format("服务%s的ip不可达。", service));
                continue;
            }

            addSyslogClient(ip, port, protocol);
        }
    }

    private boolean isReachable(String ip){
        InetAddress address;
        try {
            address = InetAddress.getByName(ip);
            //是否能通信 返回true或false
            boolean reachable = address.isReachable(3000);
            //logger.info(String.format("检查ip：%s是否可达，结果为：%s", ip, reachable));
            return reachable;
        } catch (Exception e) {
            logger.error(String.format("ip: %s 不可达。", ip), e);
        }
        return false;
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
            logger.info(String.format("添加syslogClient成功: host: %s, port: %s, protocol : %s", host, port, protocol));
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
        syslogConfig.setMaxShutdownWait(10000L);
        syslogConfig.addBackLogHandler(new Log4jBackLogHandler(Logger.getLogger(Sysloger.class)));
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
        syslogConfig.setMaxShutdownWait(10000L);
        syslogConfig.addBackLogHandler(new Log4jBackLogHandler(Logger.getLogger(Sysloger.class)));
        client.initialize("tcp", syslogConfig);
        return client;
    }

    private void clearSyslogClient(){
        Iterator<SyslogIF> iterator = this.syslogClients.iterator();
        SyslogIF next;
        while (iterator.hasNext()){
            next = iterator.next();
            next.shutdown();
            iterator.remove();
        }
        failedsyslogClients.clear();
        logger.info("清空syslogClient成功。");
    }

    @Override
    public void onEvent(DataEvent event) {
        Map<String, Object> record = event.getRecord();
        if(alarmLogFilter.isAccept(record)) {
            final String message = createMessage(record);
            getSyslogClients().forEach(entry -> {
                if(isReachable(entry.getConfig().getHost())) {
                    entry.warn(message);
                    logger.debug(String.format("使用%s协议发送告警日志数据服务器%s的端口%s成功。",
                            entry.getProtocol(), entry.getConfig().getHost(), entry.getConfig().getPort()));
                }else {
                    removeSyslogClients(entry);
                    failedsyslogClients.add(new SyslogClientInfo(entry.getConfig().getHost(), entry.getConfig().getPort(),
                            entry.getProtocol()));
                    logger.error(String.format("服务器IP:%s不可达，加入失败服务器列表中。", entry.getConfig().getHost()));
                }
            });
            logger.debug(String.format("发送告警日志的数目为：%s", sendCount.incrementAndGet()));
        }else{
            logger.debug(String.format("告警信息不满足要求：\n\t%s", event));
        }
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

    @Override
    public void shutdown(long total) {
        super.shutdown(total);
        this.failedClientCheckTimer.cancel();
    }

    private class SyslogClientInfo{
        private String host;
        private int port;
        private String protocol;

        public SyslogClientInfo(String host, int port, String protocol) {
            this.host = host;
            this.port = port;
            this.protocol = protocol;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("host='").append(host).append('\'');
            sb.append(", port=").append(port);
            sb.append(", protocol='").append(protocol).append('\'');
            return sb.toString();
        }
    }
}
