package cn.skyeye.cascade;

import cn.skyeye.common.databases.DBCommon;
import cn.skyeye.common.databases.DBConnectEntity;
import cn.skyeye.common.databases.DBType;
import cn.skyeye.common.databases.SQLites;
import cn.skyeye.resources.ConfigDetail;
import cn.skyeye.resources.Resources;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/12 17:40
 */
public class CascadeConf extends ConfigDetail {
    private final static String _CONFIG = "/cascade_config/cascade";
    protected final Logger logger = Logger.getLogger(CascadeConf.class);

    private static final Map<String, String> ENVCONF = Maps.newHashMap();

    private final int port = 60089;
    private Connection conn;
    //系统配置表
    private String systemConfigTableName;
    //级联表
    private String cascadeTableName;

    private Map<String, String> systemConfig = Maps.newConcurrentMap();
    private boolean autoFlushSystemConfig;
    private boolean sqlite;

    private ReentrantLock lock = new ReentrantLock();

    static {
        System.getProperties().forEach((keyObj, valueObj) -> {
            String key = keyObj.toString().toLowerCase();
            String value = valueObj.toString();
            if(key.startsWith("cascade.")){
                ENVCONF.put(key, value);
            }
        });
    }

    public CascadeConf(){
        super();
        this.configMap.putAll(ENVCONF);
        try {
            Resources resources = new Resources(Resources.Env.NONE, _CONFIG);
            this.configMap.putAll(resources.getConfigMap());
        } catch (Exception e) {
            logger.error("读取cascade基础配置失败。", e);
        }
        this.systemConfigTableName = getConfigItemValue("cascade.systemconfig.tablename", "system_config");
        this.cascadeTableName = getConfigItemValue("cascade.nodes.tablename", "cascade_nodes");
        this.autoFlushSystemConfig = getConfigItemBoolean("cascade.systemconfig.auto.flush", false);
    }

    private void initSystemConfigConn(){
        String dbtype = getConfigItemValue("cascade.systemconfig.db.type", "postgress").toLowerCase();
        switch (dbtype){
            case "sqlite":
                initSqliteConn();
                this.sqlite = true;
                return;
            case "postgress":
                initDataBaseConn(DBType.POSTGRESQL, dbtype);
                return;
            case "mysql":
                initDataBaseConn(DBType.MYSQL, dbtype);
                return;
            default:
                throw new IllegalArgumentException(String.format("不支持的数据库类型：%s。", dbtype));
        }
    }

    private void initSqliteConn(){
        String dbPath = getConfigItemValue("cascade.systemconfig.sqlite.file",
                "/opt/work/web/xenwebsite/data/update.db");
        Preconditions.checkArgument(new File(dbPath).exists(), String.format("sqlite的数据库文件%s不存在。", dbPath));
        try {
            this.conn = SQLites.getConn(dbPath);
            logger.info(String.format("获取与sqlite数据库文件%s的连接成功。", dbPath));
        } catch (Exception e) {
            logger.error(String.format("获取与sqlite数据库文件%s的连接失败。", dbPath), e);
        }

    }

    private void initDataBaseConn(DBType dbType, String name){
        this.sqlite = false;
        String itemName = String.format("cascade.systemconfig.%s.ip", name);
        String ip = getConfigItemValue(itemName);
        Preconditions.checkNotNull(ip, String.format("%s 不能为空。", itemName));

        itemName = String.format("cascade.systemconfig.%s.port", name);
        String port = getConfigItemValue(itemName, dbType.getDefaultPort());

        itemName = String.format("cascade.systemconfig.%s.user", name);
        String user = getConfigItemValue(itemName);
        Preconditions.checkNotNull(ip, String.format("%s 不能为空。", itemName));

        itemName = String.format("cascade.systemconfig.%s.pass", name);
        String pass = getConfigItemValue(itemName);
        Preconditions.checkNotNull(ip, String.format("%s 不能为空。", itemName));

        itemName = String.format("cascade.systemconfig.%s.db", name);
        String db = getConfigItemValue(itemName);
        Preconditions.checkNotNull(ip, String.format("%s 不能为空。", itemName));

        DBConnectEntity entity = new DBConnectEntity(dbType, db, ip, port, user, pass);
        try {
            this.conn = DBCommon.getConn(entity);
            logger.info(String.format("获取数据连接成功：\n\t %s", entity));
        } catch (Exception e) {
            logger.error(String.format("获取数据连接失败：\n\t %s", entity), e);
        }
    }

    public synchronized Connection getConn() {
        try {
            if(conn == null || !conn.isValid(3)){
                logger.debug("数据库连接失效，重新连接...");
                initSystemConfigConn();
                logger.debug("数据库重新连接成功。");
            }
        } catch (SQLException e) {
            logger.error("获取数据库连接失败。", e);
        }
        return conn;
    }

    public String getCascadeTableName() {
        return cascadeTableName;
    }

    public int getPort() {
        return port;
    }

    /**
     *  事先知道 字段列表
     */
    public void getCascadeNodes(){
        Connection conn = getConn();

    }
}
