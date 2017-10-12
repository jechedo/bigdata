package cn.skyeye.aptrules.ioc2rules;

import cn.skyeye.common.databases.DataBases;
import cn.skyeye.common.databases.SQLites;
import cn.skyeye.common.hash.Md5;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import org.apache.log4j.Logger;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/11 19:03
 */
public class IOCer {

    private final Logger logger = Logger.getLogger(IOCer.class);

    private String table = "rules";
    private String activeTimeField = "active_change_time";

    private String iocDB;
    private Connection conn;
    private List<String> columns;

    public IOCer(String iocDB) throws Exception {
        this.iocDB = iocDB;
        this.conn = SQLites.getConn(iocDB);
    }

    public int iocCount() throws Exception {

        Statement statement = getConn().createStatement();
        ResultSet resultSet = statement.executeQuery(String.format("select count(*) count from %s", table));
        int count = resultSet.getInt("count");

        DataBases.close(resultSet);
        DataBases.close(statement);
        return count;
    }

    /**
     *  统计 active_change_time 在[lower, upper]区间的数据量
     * @param lower
     * @param upper
     * @return
     * @throws SQLException
     */
    public int iocActiveBetweenCount(long lower, long upper) throws Exception {

        String sql = String.format("select count(*) count from %s where %s >= ? and %s <= ?",
                table, activeTimeField, activeTimeField);
        PreparedStatement preparedStatement = getConn().prepareStatement(sql);
        preparedStatement.setObject(1, lower);
        preparedStatement.setObject(2, upper);

        ResultSet resultSet = preparedStatement.executeQuery();
        int count = resultSet.getInt("count");

        DataBases.close(resultSet);
        DataBases.close(preparedStatement);

        return count;
    }

    /**
     * 获取所有的ioc
     */
    public void listIocs() throws Exception {
        String sql = String.format("select * from %s order by id limit 1", table);
        Statement statement = getConn().createStatement();
        ResultSet resultSet = statement.executeQuery(sql);

        if(columns == null || columns.isEmpty()){
            columns = DataBases.getColumns(resultSet);
        }

        List<Map<String, Object>> iocs = Lists.newLinkedList();
        int iocCount = 0;
        Map<String, Object> ioc;
        Object value;
        while (resultSet.next()){
            iocCount++;
            ioc = Maps.newHashMap();
           for(String column : columns){
               value = resultSet.getObject(column);
               value = changeValueByColumn(value, column);
               ioc.put(column, value);
           }

            //判断是否为有效的ioc
            boolean active = (boolean) ioc.get("active");
            boolean export = (boolean) ioc.get("export");
            int confidence = (int) ioc.get("confidence");
            if(active && export && confidence == 80){
                ioc.put("effect", 1);

                //根据type获取desc_key
                boolean success = putDescKey(ioc);
                if(!success) continue;

                //添加desc_json_key
                putDescJsonKey(ioc);

                //添加ioc_key
                putIocKey(ioc);

                iocs.add(ioc);
            }
        }

        DataBases.close(resultSet);
        DataBases.close(statement);
    }


    private Object changeValueByColumn(Object value, String column) {
        switch (column.toLowerCase()){
            case "active":
                if(value == null) value = true;
                break;
            case "export":
                if(value == null) value = true;
                break;
            case "confidence":
                if(value == null) value = 80;
                break;
            case "ip_or_domain":
                if(value != null) value = trim(String.valueOf(value));
                break;
            case "desc":
                if(value != null) value = trim(String.valueOf(value));
                break;
        }
        return value;
    }

    private boolean putDescKey(Map<String, Object> ioc){

        Object type = ioc.get("type");
        if(type == null)return false;

        String iocType = String.valueOf(type);
        String[] parts = iocType.split(":");
        String part;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < parts.length; i++){
            part = parts[i];
            if("host".equals(part)){
                parts[i] = "host_md5";
            }
            sb.append(":").append(part);
        }

        ioc.put("desc_key", sb.substring(1));

       return true;
    }

    private void putDescJsonKey(Map<String, Object> ioc){

        Object ipDomainObj = ioc.get("ip_or_domain");
        String ipDomain = "";
        if(ipDomainObj != null)ipDomain = String.valueOf(ipDomainObj).toLowerCase();

        String type = String.valueOf(ioc.get("type")).toLowerCase();
        if(type.contains("host")){
            try {
                ipDomain = Md5.Md5_32(ipDomain); //md5加密
            } catch (NoSuchAlgorithmException e) {
                logger.error(String.format("ip_or_domain: %s md5加密失败。", ipDomain), e);
            }
        }

        String descJsonKey = "";
        switch (type) {
            case "dip:dport:uri":
            case "host:dport:uri":
                descJsonKey = String.format("%s:%s", ipDomain, ioc.get("port"));
                break;
            case "host:uri":
                descJsonKey = ipDomain;
                break;
            case "dip:dport":
            case "host:dport":
                descJsonKey = String.format("%s:%s", ipDomain, ioc.get("port"));
                break;
            case "dip":
            case "host":
                descJsonKey = ipDomain;
                break;
            case "md5":
                Object md5 = ioc.get("md5");
                if (md5 != null) descJsonKey = String.valueOf(md5);
                break;
            default:
                logger.error(String.format("unexpected info_type:%s", type));
        }
        ioc.put("desc_json_key", descJsonKey);
    }

    private void putIocKey(Map<String, Object> ioc){

        Object ipDomainObj = ioc.get("ip_or_domain");
        String ipDomain = "";
        if(ipDomainObj != null)ipDomain = String.valueOf(ipDomainObj).toLowerCase();

        String type = String.valueOf(ioc.get("type")).toLowerCase();
        String iocKey = "";

        switch (type){
            case "dip:dport:uri":
            case "host:dport:uri":
                iocKey = String.format("%s:%s:%s", ipDomain, ioc.get("port"), ioc.get("url"));
                break;
            case "host:uri":
                iocKey = String.format("%s:%s", ipDomain, ioc.get("url"));
                break;
            case "dip:dport":
            case "host:dport":
                iocKey = String.format("%s:%s", ipDomain, ioc.get("port"));
                break;
            case "dip":
            case "host":
                iocKey = ipDomain;
                break;
            case "md5":
                Object md5 = ioc.get("md5");
                if(md5 != null) iocKey = String.valueOf(md5);
                break;
            default:
                logger.error(String.format("unexpected info_type:%s", type));
        }

        ioc.put("ioc_key", iocKey);
    }

    public String getIocDB() {
        return iocDB;
    }

    public String getTable() {
        return table;
    }

    private String trim(String str){
       str =  str.replaceAll("\r", "")
                .replaceAll("\n", "")
                .replaceAll("\t", "")
                .trim();
        return str;
    }

    private synchronized Connection getConn() throws Exception {
        if(conn == null || !conn.isValid(1)){
            conn = SQLites.getConn(iocDB);
        }
        return conn;
    }

    public static void main(String[] args) throws Exception {
        //IOCer ioCer = new IOCer("D:/demo/skyeye.db");
        //ioCer.listIocs();

        String localhost = Hashing.md5().hashString("localhost").toString();
        System.out.println(Md5.Md5_16("localhost"));
        System.out.println(Md5.Md5_32("localhost"));

        System.out.println(localhost);
    }

}
