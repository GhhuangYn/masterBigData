package hbase.counter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/18 14:17
 */
public class CounterDemo {

    private static Configuration conf = HBaseConfiguration.create();
    private static Connection connection;
    private static Admin admin;

    static {
        try {
            connection = ConnectionFactory.createConnection(conf);
            admin = connection.getAdmin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //单计数器
    @Test
    public void testCounter() throws IOException {
        Table table = connection.getTable(TableName.valueOf("counters"));
        long count = table.incrementColumnValue(
                Bytes.toBytes("20110101"), Bytes.toBytes("daily"), Bytes.toBytes("hits"), 1);
        System.out.println(count);
    }

    //多计数器
    @Test
    public void testMultiCounter() throws IOException {
        Table table = connection.getTable(TableName.valueOf("counters"));
        Increment increment = new Increment("20110101".getBytes());
        increment.addColumn(Bytes.toBytes("daily"), Bytes.toBytes("hits"), 1);
        increment.addColumn(Bytes.toBytes("daily"), Bytes.toBytes("clicks"), 1);
        increment.addColumn(Bytes.toBytes("weekly"), Bytes.toBytes("hits"), 1);
        increment.addColumn(Bytes.toBytes("weekly"), Bytes.toBytes("clicks"), 1);
        Result result = table.increment(increment);
        Arrays.stream(result.rawCells()).forEach((cell -> {
            System.out.println("value:" + Bytes.toLong(CellUtil.cloneValue(cell)));
        }));
    }
}
