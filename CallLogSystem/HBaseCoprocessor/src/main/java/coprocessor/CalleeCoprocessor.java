package coprocessor;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.HBaseUtils;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @Description: 插入被叫记录的协处理器，当插入主叫记录时触发
 * @author: HuangYn
 * @date: 2018/11/7 23:52
 */
public class CalleeCoprocessor extends BaseRegionObserver {

    private Logger logger = LoggerFactory.getLogger(CalleeCoprocessor.class);

    //rowkey格式： regionNo,from,date,to,duration.flag
    @Override
    public void postPut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, Durability durability) throws IOException {

//        String currTableName = e.getEnvironment().getRegionInfo().getTable().getNameAsString();
//        logger.info("当前的表: " + currTableName);
//        if (!currTableName.equals("callLog")) {
//            return;
//        }
//        //交换主叫和被叫的rowkey
//        String originRow = Bytes.toString(put.getRow());
//
//        logger.info("行键: " + originRow);
//
//        String[] info = originRow.split(",");
//
//        //如果不是主叫就返回
//        if (!"0".equals(info[5])) {
//            return;
//        }
//
//        String from = info[1];
//        String date = info[2];
//        String to = info[3];
//        String duration = info[4];
//        String regionNo = HBaseUtils.getRegionNum(to, date);
//        //拼接被叫记录的rowkey
//        String newRow = regionNo + "," + to + "," + date + "," + from + "," + duration + "," + 1;//1表示被叫
//
//        Put newPut = new Put(Bytes.toBytes(newRow));
//        newPut.addColumn("cf2".getBytes(), "caller".getBytes(), originRow.getBytes());
//
//        put.addColumn("cf2".getBytes(), "caller".getBytes(), originRow.getBytes());

//        Table table = e.getEnvironment().getTable(TableName.valueOf(currTableName));
//        table.put(newPut);

//        put.addColumn("cf2".getBytes(), "qq".getBytes(), "hello".getBytes());

    }

    @Override
    public void prePut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, Durability durability) throws IOException {

        String currTableName = e.getEnvironment().getRegionInfo().getTable().getNameAsString();
        if (!currTableName.equals("callLog")) {
            return;
        }
        //交换主叫和被叫的rowkey
        String originRow = Bytes.toString(put.getRow());
        String[] info = originRow.split(",");

        //如果不是主叫就返回
        if (!"0".equals(info[5])) {
            return;
        }

        String from = info[1];
        String date = info[2];
        String to = info[3];
        String duration = info[4];
        //拼接被叫记录的rowkey
        String newRow = HBaseUtils.getRegionNum(to, date)
                + "," + to + "," + date + "," + from + "," + duration + "," + 1;//1表示被叫

        Put newPut = new Put(Bytes.toBytes(newRow));
        newPut.addColumn("cf2".getBytes(), "caller".getBytes(), originRow.getBytes());

        Table table = e.getEnvironment().getTable(TableName.valueOf(currTableName));
        table.put(newPut);
    }
}
