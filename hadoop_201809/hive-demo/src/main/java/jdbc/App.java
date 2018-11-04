package jdbc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hive.jdbc.HiveDriver;

import java.sql.*;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/11 21:58
 */
public class App {

    public static void main(String[] args) throws SQLException {

        DriverManager.registerDriver(new HiveDriver());
        Connection connection = DriverManager.getConnection("jdbc:hive2://node01:10000/rel");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM student_info");
        while (resultSet.next()) {
            System.out.println(resultSet.getInt(1) + ":" + resultSet.getString(2));
        }
        statement.close();
        connection.close();
    }
}
