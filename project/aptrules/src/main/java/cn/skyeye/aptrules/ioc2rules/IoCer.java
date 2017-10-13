package cn.skyeye.aptrules.ioc2rules;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ARContext;
import cn.skyeye.aptrules.ioc2rules.extracters.Extracter;
import cn.skyeye.common.databases.DataBases;
import cn.skyeye.common.hash.Md5;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *     iocs表的相关操作
 * @author LiXiaoCong
 * @version 2017/10/11 19:03
 */
public class IoCer {

    private final Logger logger = Logger.getLogger(IoCer.class);

    private String table = "iocs";
    private String activeTimeField = "active_change_time";

    private List<String> columns;

    private ARConf arConf;

    public IoCer() {
        this.arConf = ARContext.get().getArConf();
    }

    public int iocCount() throws Exception {

        Statement statement = arConf.getConn().createStatement();
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
        PreparedStatement preparedStatement = arConf.getConn().prepareStatement(sql);
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
    public void listIocs(Extracter extracter) throws Exception {
        String sql = String.format("select * from %s order by id limit 1", table);
        Statement statement = arConf.getConn().createStatement();
        ResultSet resultSet = statement.executeQuery(sql);

        if(columns == null || columns.isEmpty()){
            columns = DataBases.getColumns(resultSet);
        }

        Map<String, Object> ioc;
        Object value;
        while (resultSet.next()){
            ioc = Maps.newHashMap();
           for(String column : columns){
               value = resultSet.getObject(column);
               value = changeValueByColumn(value, column);
               ioc.put(column, value);
           }
          extracter.extract(ioc);
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

    private String trim(String str){
        str =  str.replaceAll("\r", "")
                .replaceAll("\n", "")
                .replaceAll("\t", "")
                .trim();
        return str;
    }


    public String getTable() {
        return table;
    }

    public static void main(String[] args) throws Exception {
        //IoCer ioCer = new IoCer("D:/demo/skyeye.db");
        //ioCer.listIocs();

        String localhost = Hashing.md5().hashString("localhost").toString();
        //System.out.println(Md5.Md5_16("localhost"));
        System.out.println(Md5.Md5_32("www.vswlczrnm.com"));

        //System.out.println(localhost);
    }

}
