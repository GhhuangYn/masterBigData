package hbase.filter;

import java.lang.String;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;


/**
 * @Description: 第二类过滤器：继承自FilterBase,只适用于扫描操作
 * @author: HuangYn
 * @date: 2018/9/18 10:13
 */
public class FilterBaseDemo {

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

    //单列值过滤器
    //构造器中表示的是要过滤掉的条件
    @Test
    public void testSingleColumnValueFilter() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        SingleColumnValueFilter filter = new SingleColumnValueFilter(
                Bytes.toBytes("f1"),
                Bytes.toBytes("c1"),
                CompareFilter.CompareOp.EQUAL,
//                new SubstringComparator("0055")
                Bytes.toBytes("00055")
        );

        filter.setFilterIfMissing(true);            //所有不包括参考列的行都会被过滤掉，如果没有这行，则会返回所有的行

        Scan scan = new Scan();
        scan.setFilter(filter);
        table.getScanner(scan).forEach(System.out::println);
    }

    //单列排除
    @Test
    public void testSingleColumnExcludeFilter() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        SingleColumnValueExcludeFilter filter = new SingleColumnValueExcludeFilter(
                Bytes.toBytes("f1"),
                Bytes.toBytes("c1"),                //把f1:c1过滤掉
                CompareFilter.CompareOp.EQUAL,
//                new SubstringComparator("0055")
                Bytes.toBytes("00055")
        );
        Scan scan = new Scan();
        scan.setFilter(filter);
        table.getScanner(scan).forEach(System.out::println);
    }

    //前缀过滤
    @Test
    public void testPrefixFilter() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        Filter filter = new PrefixFilter(Bytes.toBytes("row0000"));
        Scan scan = new Scan();
        scan.setFilter(filter);
        table.getScanner(scan).forEach(System.out::println);
    }

    //分页过滤
    @Test
    public void testPageFilter() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        Filter filter = new PageFilter(3);
//        Filter filter2 = new FamilyFilter(CompareFilter.CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes("f2")));
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        filterList.addFilter(filter);
//        filterList.addFilter(filter2);
        Scan scan = new Scan();
        scan.setFilter(filterList);
        table.getScanner(scan).forEach((result -> {
            System.out.println(result);
        }));
    }

    //行键过滤
    @Test
    public void testKeyOnlyFilter() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        Filter filter = new KeyOnlyFilter(true);
        Scan scan = new Scan();
        scan.setFilter(filter);
        scan.setStartRow("row01997".getBytes());
        table.getScanner(scan).forEach((result -> {
            System.out.println(new String(result.getRow()));
            System.out.println(result.getValue(Bytes.toBytes("f2"), Bytes.toBytes("c2")));
        }));
    }

    //时间过滤
    @Test
    public void testTimestampFilter() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        ArrayList<Long> list = new ArrayList<>();
        list.add(1537232720472L);
        list.add(1537232720493L);
        list.add(1537232720519L);
        Filter filter = new TimestampsFilter(list);
        Scan scan = new Scan();
        scan.setFilter(filter);
        table.getScanner(scan).forEach(System.out::println);
        System.out.println("=========");
        //设置区间
        scan.setTimeRange(1537232720465L, 1537232720497L);       //双重限定
        table.getScanner(scan).forEach(System.out::println);

    }

    //随机行过滤器
    @Test
    public void testRandomRowFilter() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        Filter filter = new RandomRowFilter(0.5f);
        Scan scan = new Scan();
        scan.setFilter(filter);
        table.getScanner(scan).forEach(System.out::println);

    }

    //跳转过滤器
    //当过滤器发现某一行的某一列需要过滤时，整行数据都会被过滤掉
    @Test
    public void testSkipFilter() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        Filter filter = new ValueFilter(CompareFilter.CompareOp.GREATER,
                new BinaryComparator(Bytes.toBytes("01995")));
        Scan scan = new Scan();
        scan.setFilter(filter);
        ResultScanner scanner = table.getScanner(scan);
        int count = 0;
        for (Result result : scanner) {
            System.out.println(result);
            count++;
        }
        System.out.println("total count: " + count);

        //使用了skipFilter包装，筛选空列的行
        Filter filter1 = new SkipFilter(filter);
        scan.setFilter(filter1);
        scanner = table.getScanner(scan);
        count = 0;
        for (Result result : scanner) {
            System.out.println(result);
            count++;
        }
        System.out.println("total count: " + count);

    }

    //跳转过滤器
    //当过滤器发现某一行的某一列需要过滤时，整行数据都会被过滤掉
    @Test
    public void testMatchAllFilter() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        Filter filter = new ValueFilter(CompareFilter.CompareOp.GREATER,
                new BinaryComparator(Bytes.toBytes("01995")));
        Scan scan = new Scan();
        scan.setFilter(filter);
        ResultScanner scanner = table.getScanner(scan);
        int count = 0;
        for (Result result : scanner) {
            System.out.println(result);
            count++;
        }
        System.out.println("total count: " + count);

        //使用了skipFilter包装，筛选空列的行
        Filter filter1 = new WhileMatchFilter(filter);
        scan.setFilter(filter1);
        scanner = table.getScanner(scan);
        count = 0;
        for (Result result : scanner) {
            System.out.println(result);
            count++;
        }
        System.out.println("total count: " + count);

    }
}
