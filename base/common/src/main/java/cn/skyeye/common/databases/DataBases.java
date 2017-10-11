package cn.skyeye.common.databases;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *  数据库相关操作
 *  取消了Connection的自动提交
 *
 *  使用的是单例模式，使用步骤：
 *   1. 首先用  get(String url, String driver, String user, String password) 或 get(DBConnectEntity entity)  实例化连接
 *   2. 后面均可采用 get() 获取单例实例
 *
 *   注意：
 *       不要多次执行 步骤1 ， 因为只会采用首次执行的结果 ，后面执行的无效
 *
 * @author LiXiaoCong
 * @version 2017/4/28 13:13
 */
public class DataBases {

    private volatile static DataBases dataBases;
    private volatile static DBConnectEntity entity;

    private Connection conn;
    private DatabaseMetaData metaData;

    private DataBases(String url, String driver, String user, String password) throws Exception {
        Class.forName(driver);
        this.conn = DriverManager.getConnection(url, user, password);
        this.metaData = conn.getMetaData();
    }

    private DataBases(Connection conn) throws Exception {
        this.conn = conn;
        this.metaData = conn.getMetaData();
    }

    /**
     * INSERT INTO tb_big_data (count, create_time, random) VALUES
     * @param tableName
     * @param columns
     */
    public InsertBatch insertBatch(String tableName, Iterable<String> columns) throws SQLException {
        return new InsertBatch(tableName, columns);
    }

    public boolean checkOrCreateTable(String tableName, Map<String, Type> fieldTypeMap) throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(tableName);
        sql.append("(");
        int i = 0;
        for(Map.Entry<String, Type> fieldType : fieldTypeMap.entrySet()){
            sql.append(fieldType.getKey())
                    .append(" ")
                    .append(fieldType.getValue().toStringBySize());
            i += 1;
            if(i < fieldTypeMap.size()){
                sql.append(",");
            }else {
                sql.append(")");
            }
        }

        return execute(sql.toString());
    }

    public List<Map<String, Object>> query(String sql) throws SQLException {

        Statement statement = newStatement();
        ResultSet resultSet = queryForResultSet(sql, statement);
        List<Map<String, Object>> rows;
        try {
            rows = readResultSet(resultSet);
        }finally {
            close(resultSet);
            close(statement);
        }
        return rows;
    }

    public ResultSet queryForResultSet(String sql, Statement statement) throws SQLException {
        return statement.executeQuery(sql);
    }

    public PreparedStatement newStatement(String sql) throws SQLException {
        return getConn().prepareStatement(sql);
    }

    public boolean execute(String sql) throws SQLException {
        Statement statement = newStatement();
        boolean execute = statement.execute(sql);
        close(statement);
        return execute;
    }

    public void setAutoCommit() throws SQLException {
        setAutoCommit(true);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        conn.setAutoCommit(autoCommit);
    }

    public void commit() throws SQLException {
        this.conn.commit();
    }

    public Statement newStatement() throws SQLException {
        return conn.createStatement();
    }

    public DatabaseMetaData getMetaData() {
        return metaData;
    }

    public void close(){
        close(conn);
    }

    public Connection getConn() {
        try {
            if(conn != null && !conn.isValid(5)){
                newConn();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    private void newConn() throws ClassNotFoundException, SQLException {
        DBType type = entity.getType();
        Class.forName(type.getDriver());
        String url = type.getUrl(entity.getHost(), entity.getPort(), entity.getName());
        this.conn = DriverManager.getConnection(url, entity.getUser(), entity.getPwd());
    }


    public static DataBases get(Connection conn) throws Exception {
        if(dataBases == null){
            synchronized (DataBases.class){
                if(dataBases == null){
                    dataBases = new DataBases(conn);
                }
            }
        }
        return dataBases;
    }

    public static DataBases get(String url, String driver, String user, String password) throws Exception {
        if(dataBases == null){
            synchronized (DataBases.class){
                if(dataBases == null){
                    dataBases = new DataBases(url, driver, user, password);
                }
            }
        }
        return dataBases;
    }

    public static DataBases get(DBConnectEntity entity) throws Exception {
        DBType type = entity.getType();
        String url = type.getUrl(entity.getHost(), entity.getPort(), entity.getName());
        DataBases.entity = entity;
        return get(url, type.getDriver(), entity.getUser(), entity.getPwd());
    }

    public static DBConnectEntity getEntity() {
        return entity;
    }

    public static DataBases get(){
        return dataBases;
    }

    public static Map<String, Object> readResult(ResultSet rs, Iterable<String> columns) throws SQLException {

        Map<String, Object> row = Maps.newHashMap();

        String field;
        Object value;
        Iterator<String> iterator = columns.iterator();
        while(iterator.hasNext()){
            field = iterator.next();
            value = rs.getObject(field);
            row.put(field, value);
        }

        return row;
    }

    public static List<Map<String, Object>> readResultSet(ResultSet rs, Iterable<String> columns) throws SQLException {

        List<Map<String, Object>> rows = Lists.newArrayList();
        while (rs.next()){
            rows.add(readResult(rs, columns));
        }
        return rows;
    }

    public static List<Map<String, Object>> readResultSet(ResultSet rs) throws SQLException {

        Iterable<String> columns = null;

        List<Map<String, Object>> rows = Lists.newArrayList();
        while (rs.next()){
            if(columns == null) columns = getColumns(rs);
            rows.add(readResult(rs, columns));
        }
        return rows;
    }

    /**
     * 获取字段名称
     * @throws SQLException
     */
    public static List<String> getColumns(ResultSet rs) throws SQLException {

        List<String> columns = null;
        if(rs != null) {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            columns = Lists.newArrayListWithExpectedSize(columnCount);

            String columnLabel;
            for (int i = 1; i <= columnCount; i++) {
                columnLabel = md.getColumnLabel(i);
                columns.add(columnLabel);
            }
        }
        return columns;
    }

    /**
     * 获取字段名称和字段的类型名称
     * @throws SQLException
     */
    public static Map<String, String> getColumnAndTypeNameMap(ResultSet rs) throws SQLException {

        Map<String, String> map = null;
        if(rs != null) {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            map = Maps.newHashMapWithExpectedSize(columnCount);

            String columnLabel;
            String type;
            for (int i = 1; i <= columnCount; i++) {
                columnLabel = md.getColumnLabel(i);
                type = ColumnTypes.typeName(md.getColumnType(i));
                map.put(columnLabel, type);
            }
        }
        return map;
    }

    public static Map<String, Type> getColumnAndTypeNameWithSizeMap(ResultSet rs) throws SQLException {

        Map<String, Type> map = null;
        if(rs != null) {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            map = Maps.newHashMapWithExpectedSize(columnCount);

            String columnLabel;
            String type;
            int columnDisplaySize;
            for (int i = 1; i <= columnCount; i++) {
                columnLabel = md.getColumnLabel(i);
                type = ColumnTypes.typeName(md.getColumnType(i));
                columnDisplaySize = md.getColumnDisplaySize(i);
                map.put(columnLabel, new Type(type, columnDisplaySize));
            }
        }
        return map;
    }

    /**
     * 获取字段名称和字段的类型数字
     * @throws SQLException
     */
    public static Map<String, Integer> getColumnAndTypeMap(ResultSet rs) throws SQLException {

        Map<String, Integer> map = null;
        if(rs != null) {
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            map = Maps.newHashMapWithExpectedSize(columnCount);

            String columnLabel;
            int type;
            for (int i = 1; i <= columnCount; i++) {
                columnLabel = md.getColumnLabel(i);
                type = md.getColumnType(i);
                map.put(columnLabel, type);
            }
        }
        return map;
    }

    public static void close(Connection conn){
        try {
            if(conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(Statement statement){

        try {
            if(statement != null) statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(ResultSet resultSet){

        try {
            if(resultSet != null) resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public class InsertBatch{

        private PreparedStatement statement;
        private Map<String, Integer> pos;
        private int batchSize = 1000;
        private int num = 0;

        private InsertBatch(String tableName, Iterable<String> columns) throws SQLException {

            conn.setAutoCommit(false);

            String sqlPrefix = String.format("INSERT INTO %s (%s) values ", tableName, Joiner.on(",").join(columns));
            StringBuilder sqlBuilder = new StringBuilder(sqlPrefix);
            sqlBuilder.append("(");

            pos = Maps.newHashMap();
            Iterator<String> iterator = columns.iterator();
            String field;
            int n = 1;
            while (iterator.hasNext()){
                field = iterator.next();
                pos.put(field, n);
                sqlBuilder.append("?, ");
                n = n + 1;
            }

            String sql = sqlBuilder.substring(0, sqlBuilder.lastIndexOf(",")) + ")";
            this.statement = newStatement(sql);
        }

        public void add(Map<String, Object> row) throws SQLException {

            for(Map.Entry<String, Integer> po : pos.entrySet()){
                statement.setObject(po.getValue(), row.get(po.getKey()));
            }

            statement.addBatch();
            num = num + 1;

            if(num >= batchSize){
                statement.executeBatch();
                conn.commit();
                num = 0;
            }
        }

        public void execute() throws SQLException {
            statement.executeBatch();
            conn.commit();
            DataBases.close(statement);

        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public int getBatchSize() {
            return batchSize;
        }
    }

    public static class Type{
        private String name;
        private int size;

        private Type(String name, int size){
            this.name = name;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public int getSize() {
            return size;
        }

        public String toStringBySize(){

            String field = name;
            switch (name.toUpperCase()){
                case "VARCHAR" :
                    if(size > 21845) field = "TEXT";
                    break;
                case "BINARY" :
                    if(size > 255)  field = "BLOB";
                    break;
            }
            return String.format("%s(%s)", field, size);
        }


        @Override
        public String toString() {
            return String.format("%s(%s)", name, size);
        }
    }

}

/**
 * 高效入库的另外一种方法
 * 局限在  数据类型
 * public static void insert() {
 // 开时时间
 Long begin = new Date().getTime();
 // sql前缀
 String prefix = "INSERT INTO tb_big_data (count, create_time, random) VALUES ";
 try {
 // 保存sql后缀
 StringBuffer suffix = new StringBuffer();
 // 设置事务为非自动提交
 conn.setAutoCommit(false);
 // Statement st = conn.createStatement();
 // 比起st，pst会更好些
 PreparedStatement pst = conn.prepareStatement("");
 // 外层循环，总提交事务次数
 for (int i = 1; i <= 100; i++) {
 // 第次提交步长
 for (int j = 1; j <= 10000; j++) {
 // 构建sql后缀
 suffix.append("(" + j * i + ", SYSDATE(), " + i * j
 * Math.random() + "),");
 }
 // 构建完整sql
 String sql = prefix + suffix.substring(0, suffix.length() - 1);
 // 添加执行sql
 pst.addBatch(sql);
 // 执行操作
 pst.executeBatch();
 // 提交事务
 conn.commit();
 // 清空上一次添加的数据
 suffix = new StringBuffer();
 }
 // 头等连接
 pst.close();
 conn.close();
 } catch (SQLException e) {
 e.printStackTrace();
 }
 // 结束时间
 Long end = new Date().getTime();
 // 耗时
 System.out.println("cast : " + (end - begin) / 1000 + " ms");
 }
 *
 *
 */
