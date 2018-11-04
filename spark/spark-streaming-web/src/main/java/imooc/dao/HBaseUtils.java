package imooc.dao;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/1 20:55
 */
public class HBaseUtils {

    private Configuration conf;
    private Connection connection;
    private Admin admin;

    private HBaseUtils() {
        try {
            conf = HBaseConfiguration.create();
            conf.set("hbase.zookeeper.quorum","node00:2181,node03:2181");
            connection = ConnectionFactory.createConnection(conf);
            admin = connection.getAdmin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Table getTable(String name) {
        try {
            return connection.getTable(TableName.valueOf(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HBaseUtils instance = null;

    public static synchronized HBaseUtils getInstance() {
        if (null == instance) {
            return new HBaseUtils();
        }
        return instance;
    }

    public static void main(String[] args) throws IOException {

//        Table table = HBaseUtils.getInstance().getTable("imooc_course_clickcount");
//        table.getTableDescriptor().getFamilies().forEach(System.out::println);

    }

}
