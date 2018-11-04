package group.direct;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.topology.TopologyBuilder;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/5 18:52
 */
public class WordCountApp {

    public static void main(String[] args) throws InterruptedException, InvalidTopologyException, AuthorizationException, AlreadyAliveException {

        Config config = new Config();
        config.setDebug(true);
//        config.setNumWorkers(3);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("wc-spout", new WordCountSpout(), 3).setNumTasks(1);

        //directGrouping-grouping：发送到下一级的 <其中一个> bolt
        builder.setBolt("wc-split-bolt", new WordCountSplitBolt(), 3).setNumTasks(3)
                .directGrouping("wc-spout");

        //本地模式调试
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("wc", config, builder.createTopology());
        Thread.sleep(6000);
        cluster.shutdown();

        //集群模式
//        StormSubmitter.submitTopology("wc", config, builder.createTopology());


    }
}
