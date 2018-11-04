package util;

import java.sql.*;

/**
 * @Description: 操作mysql的工具类
 * @author: HuangYn
 * @date: 2018/10/27 18:40
 */
public class MySqlUtil {

    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";
    private static final String URL = "jdbc:mysql://node00:3306/imooc?characterEncoding=UTF8";
    private static final String DRIVER = "com.mysql.jdbc.Driver";

    public static Connection getConnetcion() {

        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void release(Connection connection, PreparedStatement pst, ResultSet rs) {
        try {
            if (pst != null)
                pst.close();
            if (rs != null)
                rs.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
