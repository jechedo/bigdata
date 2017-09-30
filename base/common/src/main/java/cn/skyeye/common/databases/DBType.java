package cn.skyeye.common.databases;

import org.apache.commons.lang.StringUtils;

public enum DBType{
		
		MYSQL(){
			// default port : 3306
			public String getUrl( String host,Object port,String dbName ){
				return String.format("jdbc:mysql://%s:%s/%s"
								+ "?characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull",
						           host,getPort(port, getDefaultPort()),dbName);
			}
			public String getDriver(){
				return "com.mysql.jdbc.Driver";
			}
			public String getDefaultPort(){
				return "3306";
			}
			
		},SQLSERVER(){
			// default port : 1433
			public String getUrl( String host,Object port,String dbName ){
				return String.format("jdbc:jtds:sqlserver://%s:%s/%s", host,getPort(port, getDefaultPort()),dbName);
			}
			public String getDriver(){
				return "net.sourceforge.jtds.jdbc.Driver";
			}
			public String getDefaultPort(){
				return "1433";
			}
			
		},ORACLE(){
			// default port : 1521
			public String getUrl( String host,Object port,String dbName ){
				return String.format("jdbc:oracle:thin:@%s:%s:%s", host,getPort(port, getDefaultPort()),dbName);
			}
			public String getDriver(){
				return "oracle.jdbc.driver.OracleDriver";
			}
			public String getDefaultPort(){
				return "1521";
			}
			
		},DB2(){
			// default port : 5000
			public String getUrl( String host,Object port,String dbName ){
				return String.format("jdbc:db2://%s:%s/%s", host,getPort(port, getDefaultPort()),dbName);
			}
			public String getDriver(){
				return "com.ibm.db2.jdbc.app.DB2Driver ";
			}
			public String getDefaultPort(){
				return "5000";
			}
			
		},SYBASE(){
			// default port : 5007
			public String getUrl( String host,Object port,String dbName ){
				return String.format("jdbc:sybase:Tds:%s:%s/%s", host, getPort(port, getDefaultPort()),dbName);
			}
			public String getDriver(){
				return "com.sybase.jdbc.SybDriver";
			}
			public String getDefaultPort(){
				return "5007";
			}
			
		},POSTGRESQL(){
			// default port : 5432
			public String getUrl( String host,Object port,String dbName ){
				return String.format("jdbc:postgresql://%s:%s/%s", host, getPort(port, getDefaultPort()), dbName);
			}
			public String getDriver(){
				return "org.postgresql.Driver";
			}
			public String getDefaultPort(){
				return "5432";
			}
			
		};
		
		public static DBType newDBType(String dbTypeName){
			
			if("mysql".equalsIgnoreCase(dbTypeName)){
				return MYSQL;
			}
			
			if("sqlserver".equalsIgnoreCase(dbTypeName)){
				return SQLSERVER;
			}
			
			if("oracle".equalsIgnoreCase(dbTypeName)){
				return ORACLE;
			}
			
			if("db2".equalsIgnoreCase(dbTypeName)){
				return DB2;
			}
			
			if("sybase".equalsIgnoreCase(dbTypeName)){
				return SYBASE;
			}
			
			if("postgresql".equalsIgnoreCase(dbTypeName)){
				return POSTGRESQL;
			}
			return null;
		}
		
		public abstract String getUrl( String host,Object port,String dbName );
		public abstract String getDriver();
		public abstract String getDefaultPort();

	String getPort(Object port, String defualtPort){
		if(port == null){
			return defualtPort;
		}else{
			String s = String.valueOf(port);
			return StringUtils.isBlank(s) ? defualtPort : s;
		}
	}
		
	}
