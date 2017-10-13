package cn.skyeye.aptrules;

import cn.skyeye.common.SysEnvs;
import cn.skyeye.common.databases.SQLites;
import cn.skyeye.resources.ConfigDetail;
import cn.skyeye.resources.Resources;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.Map;

/**
 * Description:
 *  aptrules项目的配置
 * @author LiXiaoCong
 * @version 2017/10/11 17:35
 */
public class ARConf extends ConfigDetail{
    private static final String _CONFIG = "/aprules";

    private final Logger logger = Logger.getLogger(ARConf.class);

    private long customRuleIdStart;
    private long customRuleIdEnd;

    private String dbPath;
    private Connection conn;

    private Map<String, Integer> tidMap;

    ARConf(){
        //读取系统中配置
        System.getProperties().forEach((keyObj, valueObj) -> {
            String key = keyObj.toString();
            String value = valueObj.toString();
            if(key.toLowerCase().startsWith("ar.")){
                addConfig(key, value);
            }
        });
        //读取文件配置
        try {
            Resources resources = new Resources(Resources.Env.NONE, _CONFIG);
            this.configMap.putAll(resources.getConfigMap());
        } catch (Exception e) {
            logger.error("没有读取到任何的配置。", e);
            e.printStackTrace();
            System.exit(-1);
        }

        this.customRuleIdStart = 0x2000000000000001L;
        this.customRuleIdEnd = 0x20FFFFFFFFFFFFFFL;

        this.tidMap = Maps.newHashMap();
        this.tidMap.put("自定义情报告警", 0);

        initSQLiteConn();


    }

    private void initSQLiteConn() {
        //获取数据库文件路径
        String defaultDBPath = String.format("%s/skyeye.db", SysEnvs.getJarFileDirByClass(ARConf.class));
        this.dbPath = getConfigItemValue("ar.db.path", defaultDBPath);
        try {
            this.conn = SQLites.getConn(dbPath);
        } catch (Exception e) {
            logger.error(String.format("根据数据库文件%获取连接失败。", dbPath), e);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public synchronized Connection getConn() throws Exception {
        if(conn == null || !conn.isValid(1)){
            logger.warn("数据连接失效，重新连接...");
            conn = SQLites.getConn(dbPath);
            logger.warn("数据重新连接成功。");
        }
        return conn;
    }


    public long getCustomRuleIdStart() {
        return customRuleIdStart;
    }

    public long getCustomRuleIdEnd() {
        return customRuleIdEnd;
    }
}
