package hbase.demo1;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.hbase.bolt.HBaseBolt;
import org.apache.storm.hbase.bolt.mapper.SimpleHBaseMapper;
import org.apache.storm.hbase.common.ColumnList;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.util.Properties;

/**
 * @Description: HBase与storm整合，展示如何使用SimpleHBaseMapper和HBaseBolt进行hbase的数据持久化
 * @author: HuangYn
 * @date: 2018/10/5 18:52
 */
public class WordCountApp {

    public static void main(String[] args) throws InterruptedException, InvalidTopologyException, AuthorizationException, AlreadyAliveException {

        //HbaseMapper接口用于tuple和HBase数据的转换
        SimpleHBaseMapper mapper = new SimpleHBaseMapper() {

            //返回rowkey的字节数组
            @Override
            public byte[] rowKey(Tuple tuple) {
                System.out.println("rowkey: " + tuple);     //这里可以对上一级发送过来的tuple做处理
                return super.rowKey(tuple);
            }

            //返回列族的集合
            @Override
            public ColumnList columns(Tuple tuple) {
                return super.columns(tuple);
            }
        };

        //使用mapper设置Hbase与field映射关系
        mapper.withRowKeyField("word")
                .withColumnFamily("c")
                .withCounterFields(new Fields("count"));    //使用hbase自带的incr计算对c:count进行自增

        //HBaseBolt
        HBaseBolt hBaseBolt = new HBaseBolt("wc", mapper)
                .withConfigKey("HBASE_CONF");   //此处必须配置，否则会报错

        Properties props = new Properties();
        props.put("hbase.rootdir", "hdfs://node00/hbase");

        Config config = new Config();
        config.setDebug(true);
        config.setNumWorkers(3);
        config.put("HBASE_CONF", props);        //即使props没有设置任何值，也要配置这一句

        // wordSpout ==> splitBolt ==> HBaseBolt
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("word-Spout", new WordCountSpout()).setNumTasks(1);
        builder.setBolt("split-Bolt", new WordCountSplitBolt()).shuffleGrouping("word-Spout");
//        builder.setBolt("counter-Bolt", new WordCountCounterBolt()).fieldsGrouping("split-Bolt", new Fields("word"));
        builder.setBolt("hbase-Bolt", hBaseBolt).fieldsGrouping("split-Bolt", new Fields("word"));

        //本地模式调试
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("wc", config, builder.createTopology());
        Thread.sleep(6000);
        cluster.shutdown();

    }
}
