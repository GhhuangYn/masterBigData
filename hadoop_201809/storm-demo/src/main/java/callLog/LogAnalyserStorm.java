package callLog;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/5 16:12
 */
public class LogAnalyserStorm {

    public static void main(String[] args) throws InterruptedException, InvalidTopologyException, AuthorizationException, AlreadyAliveException {
        //Create Config instance for cluster configuration
        Config config = new Config();
        config.setDebug(true);

        //
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("call-log-reader-spout", new FakeCallLogReaderSpout());

        /*
         * In shuffle grouping, an equal number of tuples is distributed
         *   randomly across all of the workers executing the bolts.
         */
        builder.setBolt("call-log-creator-bolt", new CallLogCreatorBolt())
                .shuffleGrouping("call-log-reader-spout");

        /*The fields with same values in tuples are grouped together and the
         * remaining tuples kept outside. Then, the tuples with the same field values
         * are sent forward to the same worker executing the bolts.
         */
        builder.setBolt("call-log-counter-bolt", new CallLogCounterBolt())
                .fieldsGrouping("call-log-creator-bolt", new Fields("call"));

        //本地模式调试
//        LocalCluster cluster = new LocalCluster();
//        cluster.submitTopology("LogAnalyserStorm", config, builder.createTopology());
//        Thread.sleep(10000);
//        cluster.shutdown();

        //集群模式
        StormSubmitter.submitTopology("mytop2",config,builder.createTopology());

    }
}
