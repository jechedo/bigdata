package cn.skyeye.common.databases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/10/11 18:42
 */
public class SQLites {

    private SQLites(){}

    public static Connection getConn(String db) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(String.format("jdbc:sqlite:%s", db));
    }

    public static DataBases getDataBases(String db) throws Exception {
        Connection conn = getConn(db);
        return DataBases.get(conn);
    }
}
