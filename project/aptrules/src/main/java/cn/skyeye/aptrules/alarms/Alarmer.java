package cn.skyeye.aptrules.alarms;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ARUtils;
import cn.skyeye.aptrules.ioc2rules.Ruler;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Description:
 *   根据告警规则 以及数据 生成 告警表单
 * @author LiXiaoCong
 * @version 2017/10/20 15:17
 */
public class Alarmer {

    private ARConf arConf;

    public Alarmer(ARConf arConf){
        this.arConf = arConf;
    }

    public List<Alarm> createAlarm(Map<String, Object> record, Ruler.Hits hits){

        int size = hits.getHitSize();
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
    public Alarm createAlarm(Map<String, Object> record, Ruler.IndexKey ruleKey, VagueRule rule){
        Alarm alarm = new Alarm();

        String type = rule.getIoc_type();
        Map<String, Object> descJsonMap = rule.getDescJsonInMap();


        switch (type){
            case "md5":
                String fileMd5 = ARUtils.getValueByKeyInMap(record, "file_md5");
                String processMd5 = ARUtils.getValueByKeyInMap(record, "process_md5");

                boolean fileMd5Exist = (StringUtils.isNotBlank(fileMd5));
                boolean processMd5Exist = (StringUtils.isNotBlank(processMd5));

                if(fileMd5Exist){
                   descJsonMap.put("desc_key", "file_md5");
                   descJsonMap.put("rule_state", rule.getState());
                   descJsonMap.put("rule_id", rule.getRule_id());
                   descJsonMap.put("ioc", fileMd5);
                   descJsonMap.put("md5", fileMd5);
                   descJsonMap.put("name", ARUtils.getValueByKeyInMap(record, "file_name", ""));
                }

                if(processMd5Exist && !processMd5.equals(fileMd5)){
                    descJsonMap.put("desc_key", "process_md5");
                    descJsonMap.put("rule_state", rule.getState());
                    descJsonMap.put("rule_id", rule.getRule_id());
                    descJsonMap.put("ioc", processMd5);
                    //descJsonMap.put("md5", processMd5);
                    //descJsonMap.put("name", ARUtils.getValueByKeyInMap(record, "file_name", ""));
                }else if(!fileMd5Exist){
                    //判断是否存在附件中
                }
                break;

            default:
                List<String> keyArr = Lists.newArrayList(type.split(":"));
                if(keyArr.contains("host")){
                    String relaHost = ARUtils.getValueByKeyInMap(record, "host");
                }

                if(keyArr.contains("dport")){

                }

                /*
                  try:
                    desc_key_arr = rule.desc_key.split(':')
                    # logging.info("__get_actions___desc_key_arr:%s", desc_key_arr)
                    if 'dhost' in desc_key_arr:
                        rela_host = hit_src.get('host', None)
                        # if 'dport' in desc_key_arr:
                        #     dport = hit_src.get('dport', None)
                        #     rela_host = rela_host + ":" + dport
                        if rela_host:
                            cur_keys = desc_json.keys()
                            for pos_key in cur_keys:
                                cur_key = pos_key
                                pos = pos_key.find(":")
                                if pos != -1:
                                    pos_key = pos_key[0:pos]
                                if pos_key in rela_host:
                                    ioc_detail = desc_json[cur_key]
                                    break
                    if 'dhost' not in desc_key_arr:
                        ioc_detail = desc_json[
                            (":".join(str(hit_value) for hit_value in map(hit_src.get, desc_key_arr))).lower()]
                    self.__add_uri(rule, hit, desc_key_arr)
                    if 'dhost' not in desc_key_arr:
                        desc_key_arr = [key.replace("host_md5", 'host') for key in desc_key_arr]
                    else:
                        desc_key_arr = [key.replace("dhost", 'host') for key in desc_key_arr]
                    ioc = (":".join(str(hit_value) for hit_value in map(hit_src.get, desc_key_arr))).lower()
                    # logging.info("__get_actions___ioc:%s", ioc)

                    # if host is 'www.abc.com:8080' while host_md5 is for 'www.abc.com',remove the double dport
                    if 'dport' in desc_key_arr:
                        dport = str(hit_src.get('dport')).lower()
                        ioc = ioc.replace(':%s:%s' % (dport, dport), ':%s' % dport)
                    # if uri in ioc, remove ':' from ioc
                    if 'uri' in desc_key_arr:
                        uri = str(hit_src.get('uri')).lower()
                        if uri.startswith('/'):
                            ioc = ioc.replace(':%s' % uri, uri)
                        else:
                            ioc = ioc.replace(':%s' % uri, '/%s' % uri)
                        ioc_detail['desc_key'] = '%s:%s' % (rule.desc_key, 'uri')
                    else:
                        ioc_detail['desc_key'] = rule.desc_key
                    ioc_detail['rule_state'] = rule.state
                    ioc_detail['rule_id'] = rule.rule_id  # for black_gl
                    ioc_detail['ioc'] = ioc
                    # logging.info("__get_actions___ioc_detail:%s", ioc_detail)
                    ioc_detail_list.append(ioc_detail)
            except Exception, e:
                logging.error("desc_key: rule_id=%s,hit=%s, Exception=%s" % (str(rule.rule_id), str(hit), str(e)),
                              exc_info=1)
                if not ioc_detail_list:
                    continue

                 */
        }



        return alarm;
    }
}
