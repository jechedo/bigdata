package cn.skyeye.aptrules.alarms;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ARContext;
import cn.skyeye.aptrules.Record;
import cn.skyeye.aptrules.ioc2rules.Ruler;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
import cn.skyeye.common.hash.Md5;
import cn.skyeye.common.json.Jsons;
import cn.skyeye.elasticsearch.ElasticsearchContext;
import cn.skyeye.elasticsearch.searchs.SearchCenter;
import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import org.apache.log4j.Logger;
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
    private AlarmLRUCache alarmCache;
    private SearchCenter esSearcher;

    public Alarmer(ARConf arConf){
        this.arConf = arConf;
        this.alarmCache = new AlarmLRUCache(100000);
        this.esSearcher = ElasticsearchContext.get().getSearcher();
    }

    public List<Alarm> createAlarm(Map<String, Object> data, Ruler.Hits hits){
        int size = hits.getHitSize();
        Record record = new Record(data);
        List<Alarm> alarms = Lists.newArrayListWithCapacity(size);
        for (Map.Entry<Ruler.IndexKey, VagueRule> entry : hits.getHitSet()) {
            alarms.add(createAlarm(record, entry.getKey(), entry.getValue()));
        }
        return alarms;
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
        if(!isRepeatAlarm(iocDetailMap)){
            alarm = createAlarm(record, ruleKey, iocDetailMap);
        }
        return alarm;
    }

    private Alarm createAlarm(Record record , Ruler.IndexKey ruleKey,  Map<String, Object> iocDetailMap){
        Alarm alarm = new Alarm(ruleKey.getRuleDataKey(), record.getRecord()); // Alarm的id还有待验证

        String[] victimAndAttackIP = getVictimAndAttackIP(record);
        String hostMd5 = record.getString("host_md5", "");
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
        String accessTime = record.getStringOnce("", arConf.getTimeFields());




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
                    //附件的操作相关
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
     * @param iocDetailMap
     * @return
     */
    private boolean isRepeatAlarm(Map<String, Object> iocDetailMap){

        return false;
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

    private void getFirstAccessTime(String nid, String victimIP){

        String key = String.format("%s%s", nid, victimIP);
        Alarm alarm = alarmCache.get(key);
        if(alarm == null){

        }

        /*
         key = str(nid) + alarm_sip
        oldest_alarm = self.oldest_alarm.get(key, None)
        ret_data = None
        if oldest_alarm:
            ret_data = oldest_alarm
        else:
            # get the oldest alarm by real query alarm_collection
            if alarm_sip:
                q_str = 'nid:"%s" AND alarm_sip:"%s"' % (str(nid), alarm_sip)
            else:
                q_str = 'nid:"%s"' % (str(nid),)
            query = self.__make_query('alarm_collection', 'alarm_collection', query_str=q_str, doc_size=1)

            result = query.sort('access_time').execute()
            item = result.hits.hits
            if item:
                ret_data = item[0]
                self.oldest_alarm.setdefault(key, ret_data)
        return ret_data
         */

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
