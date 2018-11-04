package hbase.demo2;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.hbase.bolt.HBaseBolt;
import org.apache.storm.hbase.bolt.HBaseLookupBolt;
import org.apache.storm.hbase.bolt.mapper.HBaseValueMapper;
import org.apache.storm.hbase.bolt.mapper.SimpleHBaseMapper;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.ITuple;
import org.apache.storm.tuple.Values;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @Description: HBase与storm整合: HBaseLookupBolt可以用于从hbase查找出数据再进行分发
 * @author: HuangYn
 * @date: 2018/10/5 18:52
 */
public class WordCountApp {

    public static void main(String[] args) throws InterruptedException, InvalidTopologyException, AuthorizationException, AlreadyAliveException {

        HBaseValueMapper hBaseValueMapper = new HBaseValueMapper() {

            //能同时获取上一级的tuple，和指定hbase的result
            //通过tuple的field和simpleHBaseMapper中配置的row的映射，从数据库中查出结果
            //simpleHBaseMapper配置了rowkey是word，因此这里是根据word从hbase中查找出一行的结果的
            @Override
            public List<Values> toValues(ITuple input, Result result) throws Exception {
                System.out.println("=====================");
                System.out.println(input);
                System.out.println("row:" + new String(result.getRow()));
                System.out.println("cf:" + new String(result.listCells().get(0).getFamily()));
                System.out.println("value:" + Bytes.toLong(result.getValue(Bytes.toBytes("c"), Bytes.toBytes("count"))));
                long count = Bytes.toLong(result.getValue(Bytes.toBytes("c"), Bytes.toBytes("count")));

                //输出values
                return Arrays.asList(new Values(new String(result.getRow()), count));
            }

            @Override
            public void declareOutputFields(OutputFieldsDeclarer declarer) {
                declarer.declare(new Fields("word", "count"));
            }
        };

        SimpleHBaseMapper simpleHBaseMapper = new SimpleHBaseMapper()
                .withRowKeyField("word")
                .withColumnFamily("c")
                .withColumnFields(new Fields("count")); //列族

        //HBaseLookupBolt会使用SimpleHBaseMapper中的rowkey查找数据，利用HBaseValueMapper获取这些值，然后进行emit
        HBaseLookupBolt lookupBolt = new HBaseLookupBolt("wc", simpleHBaseMapper, hBaseValueMapper)
                .withConfigKey("HBASE_CONF");

        Properties props = new Properties();
        props.put("hbase.rootdir", "hdfs://node00/hbase");

        Config config = new Config();
        config.setDebug(true);
        config.setNumWorkers(3);
        config.put("HBASE_CONF", props);        //即使props没有设置任何值，也要配置这一句

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("word-Spout", new WordCountSpout()).setNumTasks(1);
        builder.setBolt("lookup-Bolt", lookupBolt).shuffleGrouping("word-Spout");
        builder.setBolt("counter-bolt", new WordCountCounterBolt()).noneGrouping("lookup-Bolt");

        //本地模式调试
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("wc", config, builder.createTopology());
        Thread.sleep(6000);
        cluster.shutdown();

    }
}
