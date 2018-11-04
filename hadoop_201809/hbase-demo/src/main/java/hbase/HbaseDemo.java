package hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;


/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/17 19:00
 */
public class HbaseDemo {

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

    //设置TTL  time to live
    @Test
    public void createTableByTTL() throws IOException {
        HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf("t2"));
        HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(Bytes.toBytes("cf1"));
//        hColumnDescriptor.setTimeToLive(7);     //设置存活时间
        hColumnDescriptor.setKeepDeletedCells(KeepDeletedCells.TRUE);
        hTableDescriptor.addFamily(hColumnDescriptor);
        admin.createTable(hTableDescriptor);
        admin.close();
    }


    @Test
    public void testScanner() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        Scan scan = new Scan();
        scan.setStartRow(Bytes.toBytes("00011"));
        scan.setStopRow(Bytes.toBytes("00014"));
        ResultScanner resultScanner = table.getScanner(scan);
        resultScanner.forEach((result -> {
            result.getColumnCells(Bytes.toBytes("f1"), Bytes.toBytes("c1")).forEach(System.out::println);
        }));
    }

    //原生扫描：所有的数据都会被扫描出来，包括已经删除的记录
    @Test
    public void testRawScanner() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        Scan scan = new Scan();
        scan.setRaw(true);          //原生扫描
        scan.setStartRow("row00010".getBytes());
        scan.setStopRow("row00014".getBytes());
        scan.addFamily(Bytes.toBytes("f1"));
        ResultScanner scanner = table.getScanner(scan);
        scanner.forEach(result -> {
            result.listCells().forEach((cell -> {
                System.out.print(new String(CellUtil.cloneRow(cell)) + ": ");
                System.out.print(cell.getTimestamp() + ": ");
                System.out.println(new String(CellUtil.cloneValue(cell)));
            }));
        });
    }

    @Test
    public void testGet() throws IOException {
        Table table = connection.getTable(TableName.valueOf("testtable"));
        Get get = new Get(Bytes.toBytes("row-00011"));
        get.setMaxVersions(5);
//        get.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("c1"));
//        get.setFilter()
        Result result = table.get(get);
//        System.out.println(result);
//        System.out.println(new String(result.getValue(Bytes.toBytes("f1"), Bytes.toBytes("c1"))));

        Arrays.stream(result.rawCells()).forEach((cell -> {
            System.out.println(new Date(cell.getTimestamp()));
            System.out.println(new String(CellUtil.cloneValue(cell)));
        }));
    }

    //获取某一行多版本的数据
    @Test
    public void testGetVersions() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        Get get = new Get(Bytes.toBytes("row00011"));
        get.addFamily(Bytes.toBytes("f1"));
        get.setMaxVersions(3);                  //设置版本数
        Result result = table.get(get);
        result.listCells().forEach(cell -> {
            System.out.print(new String(CellUtil.cloneRow(cell)) + ":");
            System.out.print(cell.getTimestamp() + ":");
            System.out.println(new String(CellUtil.cloneValue(cell)));
        });

    }

    @Test
    public void testPut() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        Put put = new Put("row00011".getBytes(), 1537367923495L);  //可以指定时间戳
        put.addColumn("f1".getBytes(), "c1".getBytes(), "RR".getBytes());
        table.put(put);
    }

    //插入一万数据
    @Test
    public void testMillionPut() throws IOException {
        HTable table = (HTable) connection.getTable(TableName.valueOf("t1"));
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            DecimalFormat df = new DecimalFormat();
            df.applyPattern("00000");
            String value = df.format(i);
            Put put = new Put(Bytes.toBytes("row-" + value));
            put.addColumn("c1".getBytes(), "q1".getBytes(), Bytes.toBytes(value));
            put.setWriteToWAL(false);
            table.put(put);
        }
        System.out.println(((double) (System.currentTimeMillis() - start)) / 1000);
    }

    // 添加行或删除行
    @Test
    public void testAlter() throws IOException {
//        admin.addColumn(TableName.valueOf("t1"), new HColumnDescriptor("f4"));
        admin.deleteColumn(TableName.valueOf("t1"), "f3".getBytes());
        HTableDescriptor tableDescriptor =
                admin.getTableDescriptor(TableName.valueOf("t1"));
        tableDescriptor.getFamilies().forEach(System.out::println);
    }

    @Test
    public void testCount() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
    }

    @Test
    public void testFormat() {
        DecimalFormat decimalFormat = new DecimalFormat("000000");
        String result = decimalFormat.format(123);
        System.out.println(result);
    }

    @Test
    public void testDelete() throws IOException {
        Table table = connection.getTable(TableName.valueOf("testtable"));
        Delete delete;
        for (int i = 0; i < 1000; ++i) {
            DecimalFormat df = new DecimalFormat("00000");
            delete = new Delete(Bytes.toBytes("row-" + df.format(i)));
            table.delete(delete);
        }

    }

    @Test
    public void testDelete2() throws IOException {
        Table table = connection.getTable(TableName.valueOf("testtable"));
        Delete delete = new Delete(Bytes.toBytes("23"));
        table.delete(delete);
    }

    //批量操作
    @Test
    public void testBatch() throws IOException, InterruptedException {
        Table table = connection.getTable(TableName.valueOf("t2"));
        List<Row> batch = new ArrayList<>();

        //插入
        Put put1 = new Put(Bytes.toBytes("row1"));
        put1.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("q1"), Bytes.toBytes("James"));
        batch.add(put1);

        Put put2 = new Put(Bytes.toBytes("row2"));
        put2.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("q1"), Bytes.toBytes("Bosh"));
        put2.addColumn(Bytes.toBytes("cf2"), Bytes.toBytes("q2"), Bytes.toBytes("Wade"));
        batch.add(put2);

        //查询
        Get get = new Get(Bytes.toBytes("row2"));
        get.addColumn(Bytes.toBytes("cf2"), Bytes.toBytes("q2"));
        batch.add(get);

        //删除
        Delete delete = new Delete(Bytes.toBytes("row2"));
        delete.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("q1"));
        batch.add(delete);

        //保存结果的数组
        Object[] objects = new Object[batch.size()];

        //批量操作
        table.batch(batch, objects);
        Arrays.stream(objects).forEach(System.out::println);
    }

    //缓存测试
    @Test
    public void testScanCache() throws IOException {
        long start = System.currentTimeMillis();

        Table table = connection.getTable(TableName.valueOf("testtable"));
        Scan scan = new Scan();
        //版本1.3.0中默认已经开启，设置为整型的最大值
        //scan的缓存是面向行级别的，每次next()的返回N行数据
//        scan.setCaching(100);           //4894ms
        scan.setCaching(500);           //2017ms
        table.getScanner(scan).forEach(System.out::println);

        System.out.println("total: " + (System.currentTimeMillis() - start));
        System.out.println(scan.getCaching());      //2147483647
    }

    //批量测试
    //批量可以让用户选择每次ResultScanner实例的next()返回多少列
    @Test
    public void testScanBatch() throws IOException {
        long start = System.currentTimeMillis();

        Table table = connection.getTable(TableName.valueOf("testtable"));
        Scan scan = new Scan();
        scan.setBatch(3);           //设置批量，每次调用next()，result以列返回，一次返回3列

        //每一次next()，返回的是一行中的3列，下一次再返回同一行的3列，如此类推，直到所有列返回完毕

        int resultCount = 0;

        ResultScanner resultScanner = table.getScanner(scan);
        for (Result result : resultScanner) {
            System.out.println("========调用一次next()========");
            resultCount++;
            byte[] bytes1 = result.getValue(Bytes.toBytes("colfam1"), Bytes.toBytes("q1"));
            byte[] bytes2 = result.getValue(Bytes.toBytes("colfam2"), Bytes.toBytes("q2"));
            byte[] bytes3 = result.getValue(Bytes.toBytes("colfam3"), Bytes.toBytes("q3"));
            byte[] bytes4 = result.getValue(Bytes.toBytes("colfam4"), Bytes.toBytes("q4"));
            byte[] bytes5 = result.getValue(Bytes.toBytes("colfam5"), Bytes.toBytes("q5"));
            StringBuffer sb = new StringBuffer();
            sb.append(new String(result.getRow()));
            sb.append(" --> ");
            if (bytes1 != null && bytes1.length > 0) {
                sb.append(new String(bytes1));
                sb.append(" --> ");
            }
            if (bytes2 != null && bytes2.length > 0) {
                sb.append(new String(bytes2));
                sb.append(" --> ");
            }
            if (bytes3 != null && bytes3.length > 0) {
                sb.append(new String(bytes3));
                sb.append(" --> ");
            }
            if (bytes4 != null && bytes4.length > 0) {
                sb.append(new String(bytes4));
                sb.append(" --> ");
            }
            if (bytes5 != null && bytes5.length > 0) {
                sb.append(new String(bytes5));
            }
            System.out.println(sb.toString());
        }

        System.out.println("result count:" + resultCount);
        System.out.println("total time: " + (System.currentTimeMillis() - start));
    }

    @Test
    public void testScanBatch2() throws IOException {
        long start = System.currentTimeMillis();

        Table table = connection.getTable(TableName.valueOf("testtable"));
        Scan scan = new Scan();
        scan.setBatch(3);           //设置批量，每次调用next()，result以列返回，一次返回3列

        int resultCount = 0;

        ResultScanner resultScanner = table.getScanner(scan);
        for (Result result : resultScanner) {
            System.out.println("========调用一次next()========");
            resultCount++;


            //这个map的数据格式是<colFamily,<qualifier,<timestamp,value>>>
            NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> famliyMap = result.getMap(); //返回所有的列族和列
            famliyMap.forEach((col, cell) -> {     //获取列族
                cell.forEach((q, value) -> {
                    value.forEach((t, v) -> {
                        System.out.println(Bytes.toString(col) + "/" + Bytes.toString(q) + "/" + t + "/" + Bytes.toString(v));
                    });
                });
            });
        }

        System.out.println("++++++++++++++++++++++++++++");
        System.out.println("result count:" + resultCount);
        System.out.println("total time: " + (System.currentTimeMillis() - start));
    }

}
