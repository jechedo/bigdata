package cn.skyeye.aptrules.assets;

import cn.skyeye.aptrules.alarms.Alarm;
import cn.skyeye.aptrules.alarms.stores.AlarmStore;
import cn.skyeye.aptrules.assets.stores.AssetEsStore;
import cn.skyeye.aptrules.assets.stores.AssetStore;
import cn.skyeye.common.hash.Md5;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description:
 *  资产相关
 * @author LiXiaoCong
 * @version 2017/10/26 14:52
 */
public class Asseter {

    private final Logger logger = Logger.getLogger(Asseter.class);

    private AssetStore assetStore;
    private Map<String, Integer> stateMap;
    private Set<String> fallStates; //失陷数据类型列表

    private AlarmStore alarmStore;

    public Asseter(AlarmStore alarmStore){
        this(new AssetEsStore(), alarmStore);
    }

    public Asseter(AssetStore assetStore, AlarmStore alarmStore){
        this.assetStore = assetStore;
        this.alarmStore = alarmStore;

        this.stateMap = Maps.newHashMap();
        this.stateMap.put("正常", 0);
        this.stateMap.put("可疑", 1);
        this.stateMap.put("失陷", 2);

        //可以考虑从配置中读取
        this.fallStates = Sets.newHashSet(
                "skylar-client_processdns", "skyeye-dns", "skylar-client_processsocket", "skyeye-tcpflow",
                "skyeye-udpflow", "skyeye-weblog", "webids-ids_dolog", "webids-webattack_dolog");
    }


    public Asset getAssetByAlarm(String dataType, String alarmSip, long accessTime){
        Asset res = null;
        String conditions = String.format("ip='%s' and stime <= %s and etime >= %s order by host_state_level",
                alarmSip, accessTime, accessTime);
        List<Asset> maps = assetStore.queryAsset(conditions, 1);
        if(maps != null && maps.size() > 0) res = maps.get(0);
        return checkAsset(res, dataType, alarmSip, accessTime);
    }

    private Asset checkAsset(Asset asset, String dataType, String alarmSip, long accessTime){

        String fallState = getFallSateByDataType(dataType);
        int fallStateLevel = stateMap.get(fallState);

        if(asset != null){
            int stateLevel = asset.getInt("host_state_level", 0);
            if(fallStateLevel > stateLevel){
                //告警的主机状态比资产的主机状态高
                updateAsset(asset, fallState, fallStateLevel, accessTime);
            }
        }else if(StringUtils.isNotBlank(alarmSip)){
            // 没有资产信息，并且ip不为空，则调用闻悦的add_alarm  待完成
        }
        return asset;
    }

    private Asset updateAsset(Asset asset, String fallState, int fallStateLevel, long accessTime){

        String etime = String.valueOf(asset.getLong("etime") + 1L);
        String udid = asset.getString("udid");
        String nextAssetId = Md5.Md5_32(String.format("%s%s", udid, etime));
        Asset nextAsset = assetStore.getByAssetId(nextAssetId);

        List<Asset> updates = Lists.newArrayList();
        Asset update = asset.clone();
        update.put("etime", accessTime - 1L);
        update.put("update_time", accessTime - 1L);
        updates.add(update);

        if(nextAsset != null
                && fallState.equals(nextAsset.getString("host_state", "正常"))){
            update = nextAsset.clone();
            update.put("stime", accessTime);
            updates.add(update);
        }else{
            String id = Md5.Md5_32(String.format("%s%s", udid, accessTime));
            update = new Asset(id, asset.getData());
            update.put("stime", accessTime);
            update.put("update_time", accessTime);
            if(nextAsset != null) {
                update.put("etime", nextAsset.getLong("stime") - 1L);
            }else{
                update.put("etime", 4102416000000L);
            }
            update.put("host_state", fallState);
            update.put("host_state_level", fallStateLevel);
            updates.add(update);
        }

        assetStore.updateAssets(updates);
        //更新相应的告警表单
        queryAndUpdateAlarm(updates);

        return update;
    }

    private void queryAndUpdateAlarm(List<Asset> assets){
        String conditions;
        List<Alarm> updates = Lists.newArrayList();
        List<Alarm> alarms;
        for(Asset asset : assets){
            conditions = String.format("alarm_sip='%s' and access_time >= %s and access_time <= %s",
                    asset.getString("ip"), asset.getLong("stime"), asset.getLong("etime"));
            alarms = alarmStore.queryAlarmsInStore(conditions, Integer.MAX_VALUE);
            if(alarms != null) {
                for (Alarm alarm : alarms){
                    alarm.put("_asset", asset);
                    updates.add(alarm);
                }
            }
        }
        alarmStore.updateAlarms(updates);
    }

    private String getFallSateByDataType(String dataType){
        if(fallStates.contains(dataType)){
            return "失陷";
        }else {
            return "可疑";
        }
    }
}
