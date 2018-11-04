package hbase.filter;

import java.lang.String;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.io.IOException;

/**
 * @Description:  比较过滤器
 *      public CompareFilter(final CompareOp compareOp, //比较运算符
                    final ByteArrayComparable comparator //比较器实例) {
 * @author: HuangYn
 * @date: 2018/9/18 0:30
 */
public class CompareFilterDemo {

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

    //行过滤器 -- RowFilter
    @Test
    public void testCompareFilter() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        Filter filter;

        //==================过滤器设置======================
        //RowFilter继承自CompareFilter,行过滤器
        //小于等于
//        filter = new RowFilter(CompareFilter.CompareOp.LESS_OR_EQUAL,
//                new BinaryComparator(Bytes.toBytes("00011")));

        //正则匹配
//        filter = new RowFilter(CompareFilter.CompareOp.EQUAL,
//               new RegexStringComparator("000*55"));

        //字串匹配
//        filter = new RowFilter(CompareFilter.CompareOp.EQUAL,
//                new SubstringComparator("55"));         //行键包含"55"的都匹配

        //二进制前缀匹配
        filter = new RowFilter(CompareFilter.CompareOp.EQUAL,
                new BinaryPrefixComparator(Bytes.toBytes("0001")));

        //==================过滤器设置======================

        Scan scan = new Scan();
        scan.setFilter(filter);
        ResultScanner scanner = table.getScanner(scan);         //发送过滤器数据的序列化scan，操作时在服务端进行的
        scanner.forEach(System.out::println);

    }

    //列族过滤器 -- FamlyFilter
    @Test
    public void testFamilyFilter() throws IOException {
        Filter filter = new FamilyFilter(CompareFilter.CompareOp.EQUAL,
                new BinaryComparator(Bytes.toBytes("f2")));
        Table table = connection.getTable(TableName.valueOf("t1"));
        Scan scan = new Scan();
        scan.setFilter(filter);
        table.getScanner(scan).forEach(System.out::println);

//        Get get = new Get(Bytes.toBytes("row00011"));
//        get.setFilter(filter);
////        get.addColumn(Bytes.toBytes("f2"),Bytes.toBytes("c22"));
//        Result result = table.get(get);
//        System.out.println(new String(result.value()));     //默认返回的是第一列族第一列的数据

//        Get get2 = new Get(Bytes.toBytes("00011"));
//        get2.addFamily(Bytes.toBytes("f1"));                   //再添加一个列族，结果为0，因为过滤了f2之后，已经没有f1了
//        get2.setFilter(filter);
//        result = table.get(get2);
//        System.out.println(result.size());

    }

    //列限定符过滤器--QualifierFilter
    @Test
    public void testQualifierFilter() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        Filter qualifierfilter = new QualifierFilter(
                CompareFilter.CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes("c2")));
//        Filter rowfilter = new RowFilter(CompareFilter.CompareOp.LESS_OR_EQUAL,
//                new BinaryComparator(Bytes.toBytes("row00011")));
        Scan scan = new Scan();
        scan.setFilter(qualifierfilter);
        table.getScanner(scan).forEach((result -> {
            System.out.println(new String(result.value()) + ":" + result);
        }));
    }

    //值过滤器-- ValueFilter
    @Test
    public void testValueFilter() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        Filter valueFilter = new ValueFilter(
                CompareFilter.CompareOp.LESS, new BinaryComparator(Bytes.toBytes("00011")));
        Scan scan = new Scan();
        scan.setFilter(valueFilter);
        table.getScanner(scan).forEach((result -> {
            System.out.println(new String(result.value()) + ":" + result);
        }));
    }

    //参考列过滤器-- DependentColumnFilter
    //相当于一个 ValueFilter和一个时间戳过滤器的组合
    //过滤时，包含所有与引用时间戳相同的列，参考列的作用是提供时间戳的过滤
    @Test
    public void testDependentColumnFilter() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        Filter filter = new DependentColumnFilter(
                Bytes.toBytes("f1"), Bytes.toBytes("c1"), false, CompareFilter.CompareOp.EQUAL, new SubstringComparator("0005"));
        Scan scan = new Scan();
        scan.setFilter(filter);
        table.getScanner(scan).forEach((result -> {
            System.out.println(new String(result.value()) + ":" + result);
        }));
    }
}
