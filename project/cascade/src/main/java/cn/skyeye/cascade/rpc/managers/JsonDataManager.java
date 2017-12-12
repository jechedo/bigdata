package cn.skyeye.cascade.rpc.managers;

import cn.skyeye.common.json.Jsons;
import cn.skyeye.rpc.netty.transfers.messages.JsonMessageManager;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/12 17:51
 */
public class JsonDataManager implements JsonMessageManager {
    private final Logger logger = Logger.getLogger(JsonMessageManager.class);
    public enum MessageType{
        heartbeats, esdata, dbdata, order;
        public static MessageType get(String type){
            switch (type){
                case "heartbeats" : return heartbeats;
                case "esdata" : return esdata;
                case "dbdata" : return dbdata;
                case "order" : return order;
            }
            return null;
        }
    }

    @Override
    public byte[] handleMessage(String jsonMessage) {
        try {
            Map<String, Object> message = Jsons.toMap(jsonMessage);
            switch (String.valueOf(message.get("type")).toLowerCase()){
                case "heartbeats" : break;
                case "esdata" : break;
                case "dbdata" : break;
                case "order" : break;
            }
        } catch (Exception e) {
            logger.error(String.format("请求数据格式有误: \n\t %s", jsonMessage), e);
        }

        return new byte[0];
    }
}
