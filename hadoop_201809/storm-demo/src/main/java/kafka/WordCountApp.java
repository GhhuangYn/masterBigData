package kafka;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.testing.FixedTupleSpout;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.trident.testing.FixedBatchSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.Arrays;
import java.util.Properties;

/**
 * @Description: 整合kafka
 * @author: HuangYn
 * @date: 2018/10/5 18:52
 */
public class WordCountApp {


    public static void main(String[] args) throws InterruptedException, InvalidTopologyException, AuthorizationException, AlreadyAliveException {

        //请单独启动kafka的producer生产数据

        //============kafkaSpout设置===============
        //这个spout作为kafka的”消费者“存在

//        KafkaSpoutConfig.Builder<String, String> kafkaBuilder =
//                KafkaSpoutConfig.builder("node00:9092,node01:9092", "test1");
//        kafkaBuilder.setProp("group.id","g1");
//        kafkaBuilder.setProp("auto.offset.reset","latest");
//        kafkaBuilder.setProp("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
//        kafkaBuilder.setProp("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
//        KafkaSpoutConfig<String, String> kafkaSpoutConfig = new KafkaSpoutConfig(kafkaBuilder);
//        KafkaSpout<String, String> kafkaSpout = new KafkaSpout<>(kafkaSpoutConfig);

//        TopologyBuilder builder = new TopologyBuilder();
//        builder.setSpout("kafka-spout", kafkaSpout, 3).setNumTasks(2);
//        builder.setBolt("kafka-bolt", new WordCountSplitBolt(), 3).setNumTasks(3)
//                .noneGrouping("kafka-spout");

        //============End=============


//        //===kafkaBolt设置:这里的bolt是作为kafka的”生产者“===
        KafkaBolt<String, String> kafkaBolt = new KafkaBolt<>();
        Properties props = new Properties();
        props.put("bootstrap.servers", "node00:9092,node01:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaBolt.withProducerProperties(props)
                .withTopicSelector("test1")
                .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper());


        Fields fields = new Fields("key", "message");       //以k-v对发送到kafka
        FixedTupleSpout spout = new FixedTupleSpout(
                Arrays.asList(new Values("a", "1"),
                        new Values("b", "2"),
                        new Values("c", "3")),
                fields);
        //============End============

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("spout", spout).setNumTasks(2);
        builder.setBolt("forwardToKafka", kafkaBolt).setNumTasks(3).noneGrouping("spout");

        Config config = new Config();
        config.setDebug(true);
        config.setNumWorkers(3);

        //本地模式调试
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("wc", config, builder.createTopology());
//        Thread.sleep(6000);
//        cluster.shutdown();


    }
}
