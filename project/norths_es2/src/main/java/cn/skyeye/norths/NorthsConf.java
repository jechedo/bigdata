package cn.skyeye.norths;

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
import java.sql.*;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Description:
 * 配置分为两部分：
 *    1. 数据库中的配置
 *    2.文件配置
 * @author LiXiaoCong
 * @version 2017/11/21 16:43
 */
public class NorthsConf extends ConfigDetail {
    private final static String _CONFIG = "/norths/norths-base";
    private final Logger logger = Logger.getLogger(NorthsConf.class);

    private static final Map<String, String> ENVCONF = Maps.newHashMap();

    private Connection conn;
    private String systemConfigTableName;
    private boolean sqlite;

    private Map<String, String> systemConfig = Maps.newConcurrentMap();
    private boolean autoFlushSystemConfig;
    private ReentrantLock lock = new ReentrantLock();

    static {
        System.getProperties().forEach((keyObj, valueObj) -> {
            String key = keyObj.toString().toLowerCase();
            String value = valueObj.toString();
            if(key.startsWith("norths.")){
                ENVCONF.put(key, value);
            }
        });
    }

    public NorthsConf(){
        super();
        this.configMap.putAll(ENVCONF);
        try {
            Resources resources = new Resources(Resources.Env.NONE, _CONFIG);
            this.configMap.putAll(resources.getConfigMap());
        } catch (Exception e) {
            logger.error("读取norths基础配置失败。", e);
        }
        this.systemConfigTableName = getConfigItemValue("norths.systemconfig.tablename", "system_config");
        this.autoFlushSystemConfig = getConfigItemBoolean("norths.systemconfig.auto.flush", false);
        initSystemConfigConn();
        searchSystemConfig();
    }

    public NorthsConf(Map<String, String> conf){
        super();
        this.configMap.putAll(ENVCONF);
        if(conf != null)this.configMap.putAll(conf);
        this.systemConfigTableName = getConfigItemValue("norths.systemconfig.tablename", "system_config");
        this.autoFlushSystemConfig = getConfigItemBoolean("norths.systemconfig.auto.flush", false);
        initSystemConfigConn();
        searchSystemConfig();
    }

    private void initSystemConfigConn(){
        String dbtype = getConfigItemValue("norths.systemconfig.db.type", "sqlite").toLowerCase();
        switch (dbtype){
            case "sqlite":
                initSqliteConn();
                this.sqlite = true;
                break;
            case "postgress":
                initDataBaseConn(DBType.POSTGRESQL, dbtype);
                break;
            case "mysql":
                initDataBaseConn(DBType.MYSQL, dbtype);
                break;
            default:
                throw new IllegalArgumentException(String.format("不支持的数据库类型：%s。", dbtype));
        }
    }

    private void initSqliteConn(){
        String dbPath = getConfigItemValue("norths.systemconfig.sqlite.file",
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
        String itemName = String.format("norths.systemconfig.%s.ip", name);
        String ip = getConfigItemValue(itemName);
        Preconditions.checkNotNull(ip, String.format("%s 不能为空。", itemName));

        itemName = String.format("norths.systemconfig.%s.port", name);
        String port = getConfigItemValue(itemName, dbType.getDefaultPort());

        itemName = String.format("norths.systemconfig.%s.user", name);
        String user = getConfigItemValue(itemName);
        Preconditions.checkNotNull(ip, String.format("%s 不能为空。", itemName));

        itemName = String.format("norths.systemconfig.%s.pass", name);
        String pass = getConfigItemValue(itemName);
        Preconditions.checkNotNull(ip, String.format("%s 不能为空。", itemName));

        itemName = String.format("norths.systemconfig.%s.db", name);
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
            if(conn == null || !conn.isValid(1)){
                logger.warn("数据连接失效，重新连接...");
                initSystemConfigConn();
                logger.warn("数据重新连接成功。");
            }
        } catch (SQLException e) {
            logger.error("获取数据连接失败。", e);
        }
        return conn;
    }

    public String getSystemConfig(String key){
        lock.lock();
        try {
            return systemConfig.get(key);
        } finally {
            lock.unlock();
        }
    }

    public void setSystemConfig(String key, String value){
        String action = "add";
        lock.lock();
        try {
            if(systemConfig.containsKey(key)){
                action = "update";
            }
            systemConfig.put(key, value);
            flushSystemConfig(key, value, action);
        } finally {
            lock.unlock();
        }
    }

    public void deleteSystemConfig(String key){
        lock.lock();
        try {
            systemConfig.remove(key);
            flushSystemConfig(key, null, "delete");
        } finally {
            lock.unlock();
        }
    }

    /**
     * 考虑到sqlite数据库的性能 需要
     */
    private void searchSystemConfig(){
        Connection conn = getConn();
        if(conn != null){
            String sql = "select key,value from " + systemConfigTableName + " where key like 'norths_%'";
            PreparedStatement statement= null;
            ResultSet resultSet = null;
            lock.lock();
            try {
                statement = conn.prepareStatement(sql);
                resultSet = statement.executeQuery();

                systemConfig.clear();
                String key;
                String value;
                while (resultSet.next()){
                    key = resultSet.getString("key");
                    value = resultSet.getString("value");
                    systemConfig.put(key, value);
                }
            } catch (SQLException e) {
                logger.error("查询系统配置表失败。", e);
            } finally {
                DBCommon.close(null, statement, resultSet);
                if(sqlite){
                    DBCommon.close(conn);
                    this.conn = null;
                }
                lock.unlock();
            }
        }
    }

    private void flushSystemConfig(String key, String value, String action){
        if(autoFlushSystemConfig){
            PreparedStatement statement = null;
            try {
                String sql;
                switch (action){
                    case "add":
                        sql = String.format("insert into %s (key, value) values (?,?)", systemConfigTableName);
                        statement = getConn().prepareStatement(sql);
                        statement.setString(1, key);
                        statement.setString(2, value);
                        statement.execute();
                        break;
                    case "update":
                        sql = String.format("update %s set value = ?  where key = ?", systemConfigTableName);
                        statement = getConn().prepareStatement(sql);
                        statement.setString(1, value);
                        statement.setString(2, key);
                        statement.executeUpdate();
                        break;
                    case "delete":
                        sql = String.format("delete from %s where key = ?", systemConfigTableName);
                        statement = getConn().prepareStatement(sql);
                        statement.setString(1, key);
                        statement.execute();
                        break;
                }
            } catch (SQLException e) {
                logger.error(null, e);
            }finally {
                if(statement != null)DBCommon.close(statement);
                if(sqlite){
                    DBCommon.close(conn);
                    this.conn = null;
                }
            }
        }
    }

}
