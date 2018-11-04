package group.wc;

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
 * @date: 2018/10/5 18:52
 */
public class WordCountApp {

    public static void main(String[] args) throws InterruptedException, InvalidTopologyException, AuthorizationException, AlreadyAliveException {

        Config config = new Config();
        config.setDebug(true);
//        config.setNumWorkers(3);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("wc-spout", new WordCountSpout(), 3).setNumTasks(3);

        builder.setBolt("wc-split-bolt", new WordCountSplitBolt(), 3).setNumTasks(2)
                .shuffleGrouping("wc-spout");

        //设置两个分组counter分组用于计算单词数目
        //设置分组1 shuffle grouping
//        builder.setBolt("wc-count-bolt-1", new WordCountCounterBolt())
//                .shuffleGrouping("wc-split-bolt").setNumTasks(2);

        //设置分组2 field grouping
//        builder.setBolt("wc-count-bolt-2", new WordCountCounterBolt())
//                .fieldsGrouping("wc-count-bolt-1", new Fields("word")).setNumTasks(2);


        //本地模式调试
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("wc", config, builder.createTopology());
        Thread.sleep(6000);
        cluster.shutdown();

        //集群模式
//        StormSubmitter.submitTopology("wc", config, builder.createTopology());


    }
}
