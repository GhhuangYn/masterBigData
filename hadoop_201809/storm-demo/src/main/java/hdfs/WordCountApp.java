package hdfs;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;
import org.apache.storm.hdfs.bolt.format.RecordFormat;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;
import org.apache.storm.topology.TopologyBuilder;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/5 18:52
 */
public class WordCountApp {

    public static void main(String[] args) throws InterruptedException, InvalidTopologyException, AuthorizationException, AlreadyAliveException {

        // use "|" instead of "," for field delimiter
        //直接把上一级的输出Field用分割符隔开，并且自动换行
        RecordFormat format = new DelimitedRecordFormat()
                .withFieldDelimiter("|");

        // sync the filesystem after every 1k tuples
        SyncPolicy syncPolicy = new CountSyncPolicy(1000);

        // rotate files when they reach 5MB
        FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(5.0f, FileSizeRotationPolicy.Units.MB);

        FileNameFormat fileNameFormat = new DefaultFileNameFormat()
                .withPath("/storm/");

        HdfsBolt bolt = new HdfsBolt()
                .withFsUrl("hdfs://node00:8020/")
                .withFileNameFormat(fileNameFormat)
                .withRecordFormat(format)
                .withRotationPolicy(rotationPolicy)
                .withSyncPolicy(syncPolicy);


        Config config = new Config();
        config.setDebug(true);
        config.setNumWorkers(3);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("spout", new WordCountSpout());
        builder.setBolt("split-bolt", new WordCountSplitBolt()).shuffleGrouping("spout");
        builder.setBolt("hdfs-bolt", bolt).noneGrouping("split-bolt");

        //本地模式调试
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("wc", config, builder.createTopology());
        Thread.sleep(6000);
        cluster.shutdown();


    }
}
