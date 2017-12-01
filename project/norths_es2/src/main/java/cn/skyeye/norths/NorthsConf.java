package cn.skyeye.norths;

import cn.skyeye.common.databases.DBCommon;
import cn.skyeye.common.databases.DBConnectEntity;
import cn.skyeye.common.databases.DBType;
import cn.skyeye.common.databases.SQLites;
import cn.skyeye.resources.ConfigDetail;
import cn.skyeye.resources.Resources;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final static String _CONFIG = "/norths_config/norths";
    private final Log logger = LogFactory.getLog(NorthsConf.class);

    private static final Map<String, String> ENVCONF = Maps.newHashMap();

    private Connection conn;
    private String systemConfigTableName;
    private String threateTypeTableName;
    private boolean sqlite;

    private Map<String, String> systemConfig = Maps.newConcurrentMap();
    private Set<String> threats = Sets.newHashSet();
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
        this.threateTypeTableName = getConfigItemValue("norths.threattype.tablename", "threat_type_info");
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
                logger.debug("数据库连接失效，重新连接...");
                initSystemConfigConn();
                logger.debug("数据库重新连接成功。");
            }
        } catch (SQLException e) {
            logger.error("获取数据库连接失败。", e);
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
            String sql;
            PreparedStatement statement= null;
            ResultSet resultSet = null;

            Statement threatStatement = null;
            ResultSet threatResultSet = null;
            lock.lock();
            try {
                if(checkTableExist(conn)) {
                    sql = "select key,value from " + systemConfigTableName + " where key like 'norths_%'";
                    statement = conn.prepareStatement(sql);
                    resultSet = statement.executeQuery();

                    systemConfig.clear();
                    String key;
                    String value;
                    while (resultSet.next()) {
                        key = resultSet.getString("key");
                        value = resultSet.getString("value");
                        systemConfig.put(key, value);
                    }
                }
                logger.info(String.format("表%s中的配置为：\n\t %s", systemConfigTableName, systemConfig));

                sql = String.format("select cat from %s", threateTypeTableName);
                threatStatement = conn.createStatement();
                threatResultSet = threatStatement.executeQuery(sql);
                while (threatResultSet.next()) {
                    threats.add(threatResultSet.getString("cat"));
                }
                logger.info(String.format("告警类型的配置为：\n\t %s", threats));
            } catch (SQLException e) {
                logger.error("查询系统配置表失败。", e);
            } finally {
                DBCommon.close(null, statement, resultSet);
                DBCommon.close(null, threatStatement, threatResultSet);
                if(sqlite){
                    DBCommon.close(conn);
                    this.conn = null;
                }
                lock.unlock();
            }
        }
    }

    public void checkAndCloseConnection(Connection conn){
        if(sqlite){
            DBCommon.close(conn);
            this.conn = null;
        }
    }


    public List<String> getThreats() {
        return Lists.newArrayList(threats);
    }

    private boolean checkTableExist(Connection conn){
        try {
            ResultSet tables = conn.getMetaData().getTables(null, null, systemConfigTableName, null);
            if (tables.next()) {
                return true;
            }else {
                Statement statement = conn.createStatement();
                StringBuilder sql = new StringBuilder("CREATE TABLE ");
                sql.append(systemConfigTableName);
                sql.append(" (id INTEGER NOT NULL, key VARCHAR(32) NOT NULL, value VARCHAR(500), describe VARCHAR(200), PRIMARY KEY (id), UNIQUE (key))");
                statement.execute(sql.toString());
                statement.close();
                logger.warn(String.format("不存在系统配置表:%s，创建成功。", systemConfigTableName));
                return false;
            }
        } catch (SQLException e) {
            logger.error(String.format("判断表%s是否存在失败。", systemConfigTableName), e);
        }
      return true;
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
