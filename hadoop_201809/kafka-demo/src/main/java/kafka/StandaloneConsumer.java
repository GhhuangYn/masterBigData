package kafka;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @Description: 独立consumer，严格控制某个consumer固定地消费哪些分区
 * @author: HuangYn
 * @date: 2018/10/4 15:28
 */
public class StandaloneConsumer {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "node00:9092");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("enable.auto.commit", "false");
        props.put("auto.offset.reset", "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        List<TopicPartition> partitions = new ArrayList<>();
        consumer.partitionsFor("test1").forEach(partitionInfo ->
                partitions.add(new TopicPartition(partitionInfo.topic(), 0)));      //只订阅分区0的消息
        //赋予consumer访问分区的能力
        consumer.assign(partitions);

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(1000);
                records.forEach(r -> System.out.println(String.format("topic=%s,partition=%d,offset=%d",
                        r.topic(), r.partition(), r.offset())));
                consumer.commitSync();
            }
        } finally {
            consumer.commitSync();
            consumer.close();
        }
    }
}
