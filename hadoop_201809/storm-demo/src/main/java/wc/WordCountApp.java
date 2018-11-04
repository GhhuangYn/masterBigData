package wc;

import org.apache.storm.Config;
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
        config.setNumWorkers(3);        //所有节点的worker数目(进程)，应该分布在不同的节点上，每个worker开启各自的executor

        //setNumTasks(): 设置spout或者bolt的实例数
        //setSpout()第二个参数设置的是worker的工作线程数(开启多少个executor)

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("wc-spout", new WordCountSpout(), 3).setNumTasks(3);

        //开启6个bolt实例(task),3个工作进程(worker),每个worker开启1个线程(executorService),每个线程执行的任务数是2
        builder.setBolt("wc-split-bolt", new WordCountSplitBolt(), 3).setNumTasks(6)
                .shuffleGrouping("wc-spout");

        builder.setBolt("wc-count-bolt", new WordCountCounterBolt())
                .fieldsGrouping("wc-split-bolt", new Fields("word")).setNumTasks(5);


        //本地模式调试
//        LocalCluster cluster = new LocalCluster();
//        cluster.submitTopology("wc", config, builder.createTopology());
//        Thread.sleep(6000);
//        cluster.shutdown();

        //集群模式
        StormSubmitter.submitTopology("wc", config, builder.createTopology());


    }
}
