package hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/6 11:05
 */
public class HBaseDao {

    private Admin admin;
    private Configuration conf;
    private Connection connection;

    public HBaseDao() {

        try {
            conf = HBaseConfiguration.create();
            conf.set("hbase.zookeeper.qurom", "node00:2181,node03:2181");
            connection = ConnectionFactory.createConnection(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(String tableName, String row, String col, String[] qualifier, String[] values) {
        Table table = null;
        try {
            table = connection.getTable(TableName.valueOf(tableName));
            Put put = new Put(Bytes.toBytes(row));
            for (int i = 0; i < qualifier.length; i++) {
                put.addColumn(Bytes.toBytes(col), Bytes.toBytes(qualifier[i]), Bytes.toBytes(values[i]));
            }
            table.put(put);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (table != null)
                    table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        HBaseDao hBaseDao = new HBaseDao();
//        hBaseDao.save("wc", "fff", "c", "q", "hahahaha");
    }
}