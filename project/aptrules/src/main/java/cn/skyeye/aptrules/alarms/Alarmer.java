package cn.skyeye.aptrules.alarms;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ARContext;
import cn.skyeye.aptrules.ARUtils;
import cn.skyeye.aptrules.Record;
import cn.skyeye.aptrules.alarms.stores.AlarmStore;
import cn.skyeye.aptrules.assets.Asset;
import cn.skyeye.aptrules.assets.Asseter;
import cn.skyeye.aptrules.ioc2rules.Ruler;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
import cn.skyeye.common.Dates;
import cn.skyeye.common.hash.Md5;
import cn.skyeye.common.json.Jsons;
import cn.skyeye.elasticsearch.ElasticsearchContext;
import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import org.apache.log4j.Logger;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import redis.clients.jedis.Jedis;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Description:
 *   根据告警规则 以及数据 生成 告警表单
 * @author LiXiaoCong
 * @version 2017/10/20 15:17
 */
public class Alarmer {

    private static final String RULE_MATCH_IOCS_KEY = "xenadmin:rule:match:iocs";
    private static final String RULE_MATCH_DOTINFO_KEY = "xenadmin:rule:match:dotinfo:%s";

    private final Logger logger = Logger.getLogger(Alarmer.class);

    private ARConf arConf;
    private AlarmStore alarmStore;

    private Asseter asseter;

    public Alarmer(ARConf arConf, AlarmStore alarmStore){
        this.arConf = arConf;
        this.alarmStore = alarmStore;
        this.asseter = new Asseter(alarmStore);
    }

    public void checkAndReportAlarm(Map<String, Object> data, Ruler.Hits hits){
        Record record = new Record(data);
        Alarm alarm;
        for (Map.Entry<Ruler.IndexKey, VagueRule> entry : hits.getHitSet()) {
            alarm = createAlarm(record, entry.getKey(), entry.getValue());
            if(alarm != null){
                alarmStore.storeAlarm(alarm);
            }
        }
    }

    /**
     *
     * 需要根据实际情况 更换字段 以及判断是否为重复警告
     *
     * @param record
     * @param rule
     * @return
     */
    private Alarm createAlarm(Record record, Ruler.IndexKey ruleKey, VagueRule rule){
        Alarm alarm = null;
        Map<String, Object> iocDetailMap = createIocDetailMap(record, ruleKey, rule);
        //告警字段 + ioc + 当前日期时间
        String alarmId = Md5.Md5_32(String.format("%s,%s,%s",
                ruleKey.getRuleDataKey(), iocDetailMap.get("ioc"), Dates.getTodayTime()));
        if(!isRepeatAlarm(alarmId)){
            alarm = createAlarm(record, alarmId, iocDetailMap);
        }
        return alarm;
    }

    private Alarm createAlarm(Record record, String alarmId, Map<String, Object> iocDetailMap){
        Alarm alarm = new Alarm(alarmId, record.getData());

        String[] victimAndAttackIP = getVictimAndAttackIP(record);
        String hostMd5 = record.getString("host_md5", "");
        String serialNum = record.getString("serial_num", "");
        String fileName = record.getStringOnce("", "filename", "name");
        String fileMd5 = record.getStringOnce( "file_md5", "md5");

        Record iocRecord = new Record(iocDetailMap);
        String type = iocRecord.getString("type", "");
        String nid = iocRecord.getString("nid", "");
        String ioc = iocRecord.getString("ioc", "");
        String descKey = iocRecord.getString("desc_key", "");
        String desc = iocRecord.getString("desc", "发现IP被恶意代码感染");
        String state = iocRecord.getString("rule_state", "green");
        String level = iocRecord.getString("level", "");

        cacheHitDetail(nid, victimAndAttackIP[0]);

       if(!"high".equals(level) && !"medium".equals(level) && level != null)
           return null;

        String sipIocDip = Md5.Md5_32(String.format("%s|%s|%s" , victimAndAttackIP[0], ioc, victimAndAttackIP[1]));
        String accessTimeStr = record.getStringOnce(ARUtils.nowTimeStr(), arConf.getTimeFields());
        String oldAccessStr = getFirstAccessTime(nid, victimAndAttackIP[0]);

        long accessTime = ARUtils.parseTimeStr(accessTimeStr);
        long oldAccessTime = ARUtils.parseTimeStr(oldAccessStr);

        String firstAccessTimeStr = accessTimeStr;
        if(oldAccessTime > -1L && oldAccessTime < accessTime){
            firstAccessTimeStr =  oldAccessStr;
        }

        Asset asset = asseter.getAssetByAlarm(record.getString("data_type"), victimAndAttackIP[0], accessTime);

        alarm.addAlarmKV("alarm_sample", 1);
        alarm.addAlarmKV("alarm_sip", victimAndAttackIP[0]);
        alarm.addAlarmKV("attack_sip", victimAndAttackIP[1]);
        alarm.addAlarmKV("host_md5", hostMd5);
        alarm.addAlarmKV("file_name", fileName);
        alarm.addAlarmKV("file_md5", fileMd5);
        alarm.addAlarmKV("type", type);
        alarm.addAlarmKV("mid", "");
        alarm.addAlarmKV("nid", nid);
        alarm.addAlarmKV("ioc", ioc);
        alarm.addAlarmKV("rule_key", descKey);
        alarm.addAlarmKV("rule_desc", desc);
        alarm.addAlarmKV("rule_state", state);
        alarm.addAlarmKV("sip_ioc_dip", sipIocDip);
        alarm.addAlarmKV("access_time", accessTimeStr);
        alarm.addAlarmKV("first_access_time", firstAccessTimeStr);
        alarm.addAlarmKV("hazard_level", 1);
        alarm.addAlarmKV("attack_org", "");
        alarm.addAlarmKV("attack_type", "");
        alarm.addAlarmKV("serial_num", serialNum);
        alarm.addAlarmKV("_asset", asset.getData());

        return alarm;
    }

    /**
     * 根据命中信息 生成告警使用 iocDetail
     * @param record
     * @param ruleKey
     * @param rule
     * @return
     */
    private Map<String, Object> createIocDetailMap(Record record, Ruler.IndexKey ruleKey, VagueRule rule) {
        Map<String, Object> descJsonMap = rule.getDescJsonInMap();
        descJsonMap.put("rule_state", rule.getState());
        descJsonMap.put("rule_id", rule.getRule_id());

        List<String> ruleFields = ruleKey.getRuleFields();

        if(ruleFields.size() == 1 && "md5".equals(ruleFields.get(0))){
            String dataField = ruleKey.getDataFieldByRuleField("md5");
            Object value = ruleKey.getDataByRuleField("md5");
            switch (dataField){
                case "file_md5":
                    descJsonMap.put("desc_key", "file_md5");
                    descJsonMap.put("ioc", value);
                    descJsonMap.put("md5", value);
                    descJsonMap.put("name", record.getString("file_name", ""));
                    break;
                case "process_md5":
                    descJsonMap.put("desc_key", "process_md5");
                    descJsonMap.put("ioc", value);
                    break;
                default:
                    //附件的操作相关  待完成
                    break;
            }
        }else {
            List<Object> ruleDatas = ruleKey.getRuleDatas();
            String ioc = Joiner.on(":").join(ruleDatas).toLowerCase();
            if(ruleFields.contains("dport")){
                Object dport = ruleKey.getDataByRuleField("dport");
                ioc = ioc.replace(String.format(":%s:%s", dport, dport), String.format(":%s", dport));
            }

            if(ruleFields.contains("uri")){
                String uri = String.valueOf(ruleKey.getDataByRuleField("uri")).toLowerCase();
                if(uri.startsWith("/")){
                    ioc = ioc.replace(String.format(":%s", uri), uri);
                }else {
                    ioc = ioc.replace(String.format(":%s", uri), String.format("/%s", uri));
                }
            }
            descJsonMap.put("desc_key", rule.getDesc_key());
            descJsonMap.put("ioc", ioc);
        }

        return descJsonMap;
    }

    /**
     * 判断是否为重复告警
     * @param alarmId
     * @return
     */
    private boolean isRepeatAlarm(String alarmId){
        return alarmStore.exist(alarmId);
    }

    /**
     * 获取受害者 和 攻击者的地址
     *    需要根据数据 获取对应的ip地址
     * @param record
     * @return
     */
    private String[] getVictimAndAttackIP(Record record){

        String sip = record.getStringOnce("","sip", "sipv6");
        String dip = record.getStringOnce("","dip", "dipv6");
        String[] addrs = record.getStringArray("addr", new String[]{""});
        String addr0 = addrs[0];

        String type = record.getString("type", "");

        switch (type){
            case "skyeye-dns":
            case "skylar-client_processdns":
                //dnstype in skylar while dns_type in skyeye
                int dnsType = record.getInt("dns_type", 0);
                if(dnsType == 0){
                    return new String[]{sip, addr0};
                }else {
                    return new String[]{dip, addr0};
                }

            case "skylar-client_imfile":
                int direction = record.getInt("direction", 0);
                if(direction == 0){
                    return new String[]{sip, dip};
                }else {
                    return new String[]{dip, sip};
                }

            case "skylar-client_mail":
                int operate = record.getInt("operate", 4);
                if(operate == 4){
                    return new String[]{sip, dip};
                }else {
                    return new String[]{dip, sip};
                }

            case "skylar-client_udisk":
                operate = record.getInt("operate", 0);
                if(operate == 0){
                    return new String[]{sip, dip};
                }else {
                    return new String[]{dip, sip};
                }

            case "skyeye-file":
                if(ARContext.get().isInternalIP(dip) && !ARContext.get().isInternalIP(sip)){
                    return new String[]{dip, sip};
                }else {
                    return new String[]{sip, dip};
                }

            default:
                    return new String[]{sip, dip};
        }

    }

    private void cacheHitDetail(String nid, String victimIP){

        //缓存nid
        Jedis jedis = ARContext.get().getJedis();
        String cur = jedis.get(RULE_MATCH_IOCS_KEY);
        Set<String> data = Sets.newHashSet();
        if(cur != null){
            try {
                data = Jsons.toSet(cur);
            } catch (Exception e) {
                logger.error(String.format("redis中key:%s对的数据不是标准的json格式。", RULE_MATCH_IOCS_KEY), e);
            }
        }

        if(data.add(nid)) jedis.set(RULE_MATCH_IOCS_KEY, Jsons.obj2JsonString(data));

        //缓存受害者ip: victimIP
        String format = String.format(RULE_MATCH_DOTINFO_KEY, victimIP);
        cur = jedis.get(format);
        Map<String, Object> hitIPMap = Maps.newHashMap();
        if(cur != null){
            try {
                hitIPMap = Jsons.toMap(cur);
            } catch (Exception e) {
                logger.error(String.format("redis中key:%s对的数据不是标准的json格式。", format), e);
            }
        }

        Record hitRecord = new Record(hitIPMap);
        JSONArray hit_ips = hitRecord.getJsonArray("hit_ips", new JSONArray());
        if(!hit_ips.contains(victimIP))hit_ips.add(victimIP);
        hitIPMap.put("hit_ips", hit_ips);

        Map<Long, String> tipMap = hitRecord.getMap("time_occur_ip", Maps.newHashMap());
        tipMap.put(System.currentTimeMillis(), victimIP);
        hitIPMap.put("time_occur_ip", tipMap);

        jedis.set(format, Jsons.obj2JsonString(hitIPMap));

    }

    private String getFirstAccessTime(String nid, String victimIP){

        String res = null;
        String key = String.format("%s:%s", nid, victimIP);
        Alarm alarm = alarmStore.getAlarmInCache(key);
        if(alarm == null) {
            String conditions = String.format("nid='%s'", nid);
            if (victimIP != null) {
                conditions = String.format("%s and alarm_sip='%s'", conditions, victimIP);
            }
            conditions = String.format("%s and alarm_sip='%s' order by access_time", conditions, victimIP);
            List<Alarm> alarms = alarmStore.queryAlarmsInStore(conditions, 1);
            if(alarms != null && alarms.size() > 0){
                alarm = alarms.get(0);
                alarmStore.addCache(key, alarm);
            }
        }

        if(alarm != null)
            res = alarm.getAlarmValue("access_time", null);

        return res;
    }

    /**
     * 获取资产
     * 缺少更新操作
     * @param victimIP
     * @param accessTime
     * @return
     */
    private Map<String, Object> getAsset(String victimIP, long accessTime){
        String eql = String.format("select * from alarm_asset/alarm_asset where " +
                "ip='%s' and stime <= %s and etime >= %s order by host_state_level desc limit 1", victimIP, accessTime, accessTime);
        SearchHits searchHits = ElasticsearchContext.get().getSearcher().searchSQLForHits(null, eql);
        SearchHit[] hits = searchHits.getHits();
        if(hits.length > 0) {
            return hits[0].sourceAsMap();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        //2017-10-24T14:11:58.942+0800
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        System.out.println(simpleDateFormat.format(new Date()));

        InetAddress inetAddress = InetAddresses.forString("172.24.66.202");

        Map<String, Object> hitIPMap = Maps.newHashMap();
        HashSet<String> sets = Sets.newHashSet("111", "sss", "dewde");
        hitIPMap.put("hit_ips", sets);

        Map<Long, String> toi = Maps.newHashMap();
        toi.put(System.currentTimeMillis(), "111");
        toi.put(1122132L, "sss");
        toi.put(43248932487328432L, "dewde");
        hitIPMap.put("time_occur_ip", toi);

        String s = Jsons.obj2JsonString(hitIPMap);
        System.out.println(s);

        Map<String, Object> res = Jsons.toMap(s);
        Record record = new Record(res);
        JSONArray hit_ips = record.getJsonArray("hit_ips");
        hit_ips.add("2354");
        hitIPMap.put("hit_ips", hit_ips);
        hit_ips.forEach(e -> System.out.print( e + " "));
        System.out.println();
        System.out.println(hit_ips + " " + hit_ips.size());

        toi = record.getMap("time_occur_ip", Maps.newHashMap());
        System.out.println(toi);

        Set<String> objects = Jsons.toSet(Jsons.obj2JsonString(sets));
        System.out.println(objects);

        s = Jsons.obj2JsonString(hitIPMap);
        System.out.println(s);
    }
}
