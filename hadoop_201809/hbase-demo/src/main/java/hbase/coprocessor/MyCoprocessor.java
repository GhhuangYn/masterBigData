package hbase.coprocessor;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/20 18:14
 */
public class MyCoprocessor extends BaseRegionObserver {

    private void writeFile(String content) throws IOException {
        FileWriter fileWriter = new FileWriter("/root/copro.txt",true);
        fileWriter.write(content);
        fileWriter.write("\r\n");
        fileWriter.close();
    }

    @Override
    public void start(CoprocessorEnvironment e) throws IOException {
        super.start(e);
        writeFile("MyCoprocessor start ...");
    }

    @Override
    public void stop(CoprocessorEnvironment e) throws IOException {
        super.stop(e);
        writeFile("MyCoprocessor stop ... Bye Bye");

    }

    @Override
    public void preOpen(ObserverContext<RegionCoprocessorEnvironment> e) throws IOException {
        super.preOpen(e);
        writeFile("MyCoprocessor preOpen .. ");
        writeFile("regionInfo: " + e.getEnvironment().getRegionInfo());
    }

    @Override
    public void postOpen(ObserverContext<RegionCoprocessorEnvironment> e) {
        super.postOpen(e);
        try {
            writeFile("MyCoprocessor postOpen .. ");
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    @Override
    public void preGetOp(ObserverContext<RegionCoprocessorEnvironment> e, Get get, List<Cell> results) throws IOException {
        super.preGetOp(e, get, results);
        System.out.println("preGet .. ");
        results.forEach((cell -> {
            try {
                writeFile("cell:" +
                        Bytes.toString(CellUtil.cloneRow(cell)) + "-" + Bytes.toString(CellUtil.cloneValue(cell)));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }));
    }

    @Override
    public void postGetOp(ObserverContext<RegionCoprocessorEnvironment> e, Get get, List<Cell> results) throws IOException {
        super.postGetOp(e, get, results);
    }

    @Override
    public void prePut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, Durability durability) throws IOException {
        super.prePut(e, put, edit, durability);
        writeFile("prePut ...");
        writeFile("serverName: "+e.getEnvironment().getRegionServerServices().getServerName());
    }

    @Override
    public void postPut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, Durability durability) throws IOException {
        super.postPut(e, put, edit, durability);
        writeFile("postPut ...");
        writeFile("serverName: "+e.getEnvironment().getRegionServerServices().getServerName());
    }

    @Override
    public void postDelete(ObserverContext<RegionCoprocessorEnvironment> e, Delete delete, WALEdit edit, Durability durability) throws IOException {
        super.postDelete(e, delete, edit, durability);
        writeFile("postDelete ...");
        writeFile("serverName:  "+e.getEnvironment().getRegionServerServices().getServerName());
    }


}
