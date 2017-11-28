package cn.skyeye.norths.services.syslog;

import cn.skyeye.common.json.Jsons;
import cn.skyeye.norths.events.DataEvent;
import cn.skyeye.norths.events.DataEventHandler;
import cn.skyeye.norths.utils.AlarmLogFilter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogIF;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/20 19:45
 */
public class Sysloger extends DataEventHandler {
    private Map<String, SyslogIF> syslogClients;
    private ReentrantLock lock = new ReentrantLock();

    private AlarmLogFilter alarmLogFilter;
    private SyslogConf syslogConf;

    public Sysloger(String name){
        super(name);
        this.syslogClients = Maps.newConcurrentMap();
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
        } finally {
            lock.unlock();
        }
    }

    private void initSyslogClient(String protocol, List<Map<String, Object>> services, boolean clear){
        if(clear)clearSyslogClient();
        Object ipObj;
        Object portObj;
        Object idObj;
        for(Map<String, Object> service : services){
            ipObj = service.get("host");
            if(ipObj == null)continue;

            portObj = service.get("port");
            if(portObj == null)continue;

            idObj = service.get("id");
            if(idObj == null)continue;

            addSyslogClient(String.valueOf(idObj),
                    String.valueOf(ipObj),
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

    public AlarmLogFilter getAlarmLogFilter() {
        lock.lock();
        try {
            return alarmLogFilter;
        } finally {
            lock.unlock();
        }
    }

    private void addSyslogClient(String id, String host, int port, String protocol){
        SyslogIF syslogClient = newSyslogClient(host, port, protocol);
        this.syslogClients.put(id, syslogClient);
    }

    private SyslogIF newSyslogClient(String host, int port, String protocol) {
        SyslogIF syslogClient = Syslog.getInstance(protocol);
        syslogClient.getConfig().setHost(host);
        syslogClient.getConfig().setPort(port);
        syslogClient.getConfig().setFacility("LOCAL3");
        syslogClient.getConfig().setSendLocalName(false);
        syslogClient.getConfig().setSendLocalTimestamp(false);
        return syslogClient;
    }

    private void clearSyslogClient(){
        this.syslogClients.clear();
        logger.info("清空syslogClient成功。");
    }

    private Map<String, SyslogIF> getSyslogClients(){
        this.lock.lock();
        Map<String, SyslogIF> map;
        try {
            map = Maps.newHashMap(syslogClients);
            return map;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void onEvent(DataEvent event) {
        Map<String, Object> record = event.getRecord();
        if(getAlarmLogFilter().isAccept(record)) {
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

        return Jsons.obj2JsonString(record);
    }

    public boolean isEmpty(){
        return this.syslogClients.isEmpty();
    }

    public SyslogConf getSyslogConf() {
        return syslogConf;
    }

    public static void main(String[] args) throws Exception {

        List<Map<String, Object>> services = Lists.newArrayList();
        Map<String, Object> service1 = Maps.newHashMap();
        service1.put("id", "001");
        service1.put("host", "127.0.0.1");
        service1.put("port", 547);

        Map<String, Object> service2 = Maps.newHashMap();
        service2.put("id", "002");
        service2.put("host", "127.0.0.8");
        service2.put("port", 547);

        services.add(service1);
        services.add(service2);

        Map<String, Object> res = Maps.newHashMap();
        res.put("switch", "0");
        res.put("protocol", "UDP");
        res.put("threat_switch", "0");
        // res.put("systemlog_switch", "0");
        res.put("services", services);

        String s = Jsons.obj2JsonString(res);

    /*    SyslogConfig syslogConfig = new SyslogConfig(Jsons.toMap(s));
        List<Map<String, Object>> services1 = syslogConfig.getServices();
        System.out.println(services1);*/
    }

}
