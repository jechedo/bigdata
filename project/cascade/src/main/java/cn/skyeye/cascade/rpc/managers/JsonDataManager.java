package cn.skyeye.cascade.rpc.managers;

import cn.skyeye.cascade.CascadeContext;
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

    private CascadeContext cascadeContext;
    public JsonDataManager(CascadeContext cascadeContext){
        this.cascadeContext = cascadeContext;
    }

    @Override
    public byte[] handleMessage(String jsonMessage) {
        try {
            Map<String, String> message = Jsons.toMap(jsonMessage);
            String type = message.get("type").toLowerCase();
            message.remove("type");
            switch (type){
                case "register" :
                    handleRegist(message);
                    break;
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

    private void handleRegist(Map<String, String> registMSG){

    }
}
