package kafka.multithread.demo1;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

/**
 * @Description: 消费线程类，执行任务
 * @author: HuangYn
 * @date: 2018/10/4 11:39
 */
public class ConsumerRunnable implements Runnable {

    //每个线程独自维护一个kafkaConsumer实例
    private final KafkaConsumer<String, String> kafkaConsumer;

    public ConsumerRunnable(String brokerList, String groupId, String topic) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", brokerList);
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("group.id", groupId);
        properties.put("enable.auto.commit", "true");            //自动提交位移
        properties.put("auto.commit.interval", "1000");          //自动提交位移的时间间隔
        properties.put("session.timeout.ms", "30000");           //consumer group检测组内成员发送崩溃的时间，也是consumer消息处理逻辑的最大时间
        this.kafkaConsumer = new KafkaConsumer<String, String>(properties);
        kafkaConsumer.subscribe(Arrays.asList(topic));      //分区副本自动分配
    }

    @Override
    public void run() {
        while (true) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(1000);
            records.forEach(r -> {
                System.out.println(Thread.currentThread().getName() + " consumed " +
                        r.partition() + " th message with offset: " + r.offset());
            });
        }
    }
}
