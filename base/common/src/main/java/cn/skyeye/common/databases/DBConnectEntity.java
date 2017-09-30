/**
 * 
 */
package cn.skyeye.common.databases;

import java.io.Serializable;

public class DBConnectEntity implements Serializable{
	
	private DBType type;
	private String name;
	private String host;
	private String port;
	private String user;
	private String pwd;

	public DBConnectEntity() {
		super();
	}

	public DBConnectEntity(DBType type, String name, String host, String port,
						   String user, String pwd) {
		super();
		this.type = type;
		
		if(type == null ){
			throw new IllegalArgumentException("数据类类型不能识别或为空!");
		}
		
		this.name = name;
		this.host = host;
		this.port = port;
		this.user = user;
		this.pwd = pwd;
	}
	
	public DBConnectEntity(String type, String name, String host, String port,
						   String user, String pwd) {
		
		this(DBType.newDBType(type) , name , host,port,user,pwd);
		
	}
	
	public DBType getType() {
		return type;
	}
	public void setType(DBType type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("DBConnectEntity{");
		sb.append("type=").append(type);
		sb.append(", name='").append(name).append('\'');
		sb.append(", host='").append(host).append('\'');
		sb.append(", port='").append(port).append('\'');
		sb.append(", user='").append(user).append('\'');
		sb.append(", pwd='").append(pwd).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
