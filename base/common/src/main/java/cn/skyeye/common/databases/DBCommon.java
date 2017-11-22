package cn.skyeye.common.databases;

import com.google.common.collect.Maps;

import java.sql.*;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DBCommon {
	
	private static Connection conn ;
	
	private DBCommon(){}

	/**
	 * @param url eg:"jdbc:oracle:thin:@172.16.1.111:1521:d406"
	 * @param driver eg:"oracle.jdbc.driver.OracleDriver"
	 * @param user eg:"ucase"
	 * @param password eg:"ucase123"
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	
	public static Connection getConn(String url, String driver, String user,
									 String password) throws ClassNotFoundException, SQLException{
		Class.forName(driver);
		conn = DriverManager.getConnection(url, user, password);
		
		return  conn;
	}
	public static Connection getConn(DBType type, String host, Object port,
					 String dbName, String userName, String password) throws ClassNotFoundException, SQLException{
		
		Class.forName(type.getDriver());
		String url = type.getUrl(host,port,dbName);
		conn = DriverManager.getConnection(url, userName, password);
		return  conn;
	}

	public static Connection getConn(DBConnectEntity entity) throws ClassNotFoundException, SQLException{
		DBType type = entity.getType();
		Class.forName(type.getDriver());
		String url = type.getUrl(entity.getHost(), entity.getPort(), entity.getName());
		conn = DriverManager.getConnection(url, entity.getUser(), entity.getPwd());
		return  conn;
	}

	/**
	 * 获取结果集中的列名称 以及type
	 * 列名全部为大写
	 * @return  Map<String, Integer>
	 *     SQL type from java.sql.Types
	 * @see Types
	 */
	public static Map<String, Integer> listColumnAndTypes(ResultSet rs) throws SQLException {

		Map<String, Integer> map = null;
		if(rs != null) {
			ResultSetMetaData md = rs.getMetaData();
			int columnCount = md.getColumnCount();
			map = Maps.newHashMapWithExpectedSize(columnCount);

			String columnLabel;
			int type;
			for (int i = 1; i <= columnCount; i++) {
				columnLabel = md.getColumnLabel(i).toUpperCase();
				type = md.getColumnType(i);
				map.put(columnLabel, type);
			}
		}
		return map;
	}


	/**
	 *  时间格式统一转成了long类型 精确到秒
	 * @param rs
	 * @param columnLabel
	 * @param type
	 * @return
	 * @throws SQLException
	 */
	public static Object readColumnValue(ResultSet rs, String columnLabel,
										 int type) throws SQLException {
		switch (type){
			case Types.ARRAY  : return rs.getArray(columnLabel);
			case Types.BIGINT : return rs.getLong(columnLabel);
			case Types.BINARY : return rs.getByte(columnLabel);
			case Types.BIT    : return rs.getBoolean(columnLabel);
			case Types.BLOB   : return rs.getBlob(columnLabel);
			case Types.BOOLEAN: return rs.getBoolean(columnLabel);
			case Types.CHAR   : return rs.getString(columnLabel);
			case Types.CLOB   : return rs.getClob(columnLabel);
			case Types.DATE   :
				final Date date = rs.getDate(columnLabel);
				if(date == null)return null;

				Calendar instance = Calendar.getInstance();
				instance.setTime(date);

				String dateStr = rs.getString(columnLabel);
				String[] split = dateStr.split(" ");
				if(split.length == 2){
					String[] hour = split[1].split(":");
					switch (hour.length){
						case 3 : instance.set(Calendar.MILLISECOND,
								(int)Double.parseDouble(hour[2])*1000);
						case 2 : instance.set(Calendar.MINUTE, Integer.parseInt(hour[1]));
						case 1 : instance.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour[0]));
							break;
					}
				}
				return instance.getTime().getTime()/1000;
			case Types.DECIMAL: return rs.getBigDecimal(columnLabel);
			case Types.DOUBLE : return rs.getDouble(columnLabel);
			case Types.FLOAT  : return rs.getFloat(columnLabel);
			case Types.INTEGER: return rs.getInt(columnLabel);
			case Types.LONGNVARCHAR : return rs.getString(columnLabel);
			case Types.LONGVARBINARY : return rs.getByte(columnLabel);
			case Types.LONGVARCHAR : return rs.getString(columnLabel);
			case Types.NCHAR : return rs.getString(columnLabel);
			case Types.NCLOB : return rs.getClob(columnLabel);
			case Types.NUMERIC  : return rs.getBigDecimal(columnLabel);
			case Types.NVARCHAR : return rs.getString(columnLabel);
			case Types.SMALLINT : return rs.getInt(columnLabel);
			case Types.TIME     :
				Time time = rs.getTime(columnLabel);
				return time == null ? null : time.getTime()/1000;
			case Types.TIMESTAMP:
				Timestamp timestamp = rs.getTimestamp(columnLabel);
				return timestamp == null ? null : timestamp.getTime()/1000;
			case Types.TINYINT : return rs.getInt(columnLabel);
			case Types.VARBINARY : return rs.getByte(columnLabel);
			case Types.VARCHAR : return rs.getString(columnLabel);

			default:
				return rs.getString(columnLabel);
		}
	}
	
	public static void close(Connection conn,Statement statement,ResultSet resultSet) {
		try {
			if( resultSet != null ){
                resultSet.close();
            }
			if( statement != null ){
                statement.close();
            }
			if( conn != null ){
                conn.close();
            }
		} catch (SQLException e) {}
	}

	public static void close(Statement statement) {
		try {
			if( statement != null ){
                statement.close();
            }
		} catch (SQLException e) {}
	}

	public static void close(ResultSet resultSet) {
		try {
			if( resultSet != null ){
                resultSet.close();
            }
		} catch (SQLException e) {}
	}

	public static void close(Connection conn) {
		try {
			if( conn != null ){
                conn.close();
            }
		} catch (SQLException e) {}
	}

	public static String createSingleMergeIntoSql(String table,
											Map<String, Object> fieldValueMap,
											Set<String> primaryKeys,
											Set<String> autoUpdateDateFields,
											Set<String> autoInsertDateFields){
		return MergeInto.createMergeIntoSql(table, fieldValueMap, primaryKeys,
				autoUpdateDateFields, autoInsertDateFields);
	}

	public static String createMergeIntoSql(String table,
												  List<Map<String, Object>> fieldValueMaps,
												  Set<String> columns,
												  Set<String> primaryKeys,
												  Set<String> autoUpdateDateFields,
												  Set<String> autoInsertDateFields){
		return MergeInto.createMergeIntoSql(table,
				fieldValueMaps,
				columns,
				primaryKeys,
				autoUpdateDateFields,
				autoInsertDateFields);
	}

	public static void commonBatch(String mergeCenterSql, String mergeBeginSql, String mergeEndSql,
								   Connection conn) throws SQLException {
		mergeCenterSql = mergeCenterSql.substring(0, mergeCenterSql.lastIndexOf("union"));
		String mergeSql = mergeBeginSql + mergeCenterSql + mergeEndSql;
		PreparedStatement statement = conn.prepareStatement(mergeSql);
		statement.executeUpdate();
		statement.close();
		conn.commit();
	}

}
