package hbase.manage;

import com.google.protobuf.ServiceException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.exceptions.DeserializationException;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;


/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/17 19:00
 */
public class AdminDemo {

    private static Configuration conf = HBaseConfiguration.create();
    private static Connection connection;
    private static Admin admin;

    static {
        try {
//            conf.set("fs.defaultFS", "hdfs://node00:8020");
//            conf.set("hbase.rootdir", "hdfs://node00:8020/hbase");
//            conf.set("hbase.cluster.distributed", "true");
//            conf.set("hbase.master", "node00:60000");
            connection = ConnectionFactory.createConnection(conf);
            admin = connection.getAdmin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetTableInfo() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        HTableDescriptor descriptor = table.getTableDescriptor();
//        admin.split(TableName.valueOf("t1"),Bytes.toBytes("row00962"));
//        descriptor.getFamilies().forEach(System.out::println);
//        System.out.println("region size:"+descriptor.getMaxFileSize());
//        System.out.println("memstore size:"+descriptor.getMemStoreFlushSize());
    }

    @Test
    public void modifyTable() throws IOException {
        TableName tableName = TableName.valueOf("t1");
        HTableDescriptor hTableDescriptor = admin.getTableDescriptor(tableName);
        hTableDescriptor.setMaxFileSize(1024 * 1024 * 5);
        HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(Bytes.toBytes("f3"));
        hTableDescriptor.addFamily(hColumnDescriptor);
        admin.disableTable(tableName);
        admin.modifyTable(tableName, hTableDescriptor);
        admin.enableTable(tableName);
    }

    //修改表结构-删除列
    @Test
    public void deleteColumn() throws IOException {
        TableName tableName = TableName.valueOf("t1");
        HTableDescriptor hTableDescriptor = admin.getTableDescriptor(tableName);
        hTableDescriptor.removeFamily(Bytes.toBytes("f3"));
        admin.modifyTable(tableName, hTableDescriptor);
    }

    //获取集群信息
    @Test
    public void testGetClusterInfo() throws IOException, ServiceException, DeserializationException {
        System.out.println(admin);
        ClusterStatus status = admin.getClusterStatus();
        System.out.println(status);
        System.out.println("-->server");
        status.getServers().forEach(serverName -> {
            System.out.println(serverName.getServerName());
            ServerLoad serverLoad = status.getLoad(serverName);
            System.out.println(serverLoad.getNumberOfRegions());
            System.out.println(serverLoad.getReadRequestsCount());
        });
        HBaseAdmin.checkHBaseAvailable(conf);
    }

    //获取Region的信息
    @Test
    public void testGetRegionInfo() throws IOException {
        RegionLocator regionLocator = connection.getRegionLocator(TableName.valueOf("t1"));
        regionLocator.getAllRegionLocations().forEach((regionLocation) -> {
            System.out.println("==========");
            HRegionInfo regionInfo = regionLocation.getRegionInfo();
            System.out.println("regionId--> " + regionInfo.getRegionId());
            System.out.println("encodedName--> " + regionInfo.getEncodedName());
            System.out.println("startKey--> " + new String(regionInfo.getStartKey()));
            System.out.println("endKey--> " + new String(regionInfo.getEndKey()));
            System.out.println("regionName--> " + regionLocation.getRegionInfo().getRegionNameAsString());
            System.out.println("regionServerName--> " + regionLocation.getServerName());
        });
    }

    //移动region
    @Test
    public void testMoveRegion() throws IOException {

        RegionLocator regionLocator = connection.getRegionLocator(TableName.valueOf("t1"));
//        admin.move(Bytes.toBytes("t1,,1537171376019.a6228d260b06a359ac0ec88bef4bde51."), Bytes.toBytes("node03"));

    }

    @Test
    public void testGetTableInfo2() throws IOException {
        Table table = connection.getTable(TableName.valueOf("t1"));
        HTableDescriptor hTableDescriptor = table.getTableDescriptor();
//        System.out.println("memStoreFlushSize:" + hTableDescriptor.getMemStoreFlushSize());   //This defaults to a size of 64 MB.


    }
}
