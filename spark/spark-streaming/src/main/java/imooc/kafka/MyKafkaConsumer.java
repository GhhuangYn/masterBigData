package imooc.kafka;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.serializer.StringDecoder;

import java.util.*;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/31 10:40
 */
public class MyKafkaConsumer extends Thread {

    private String topic;
    private ConsumerConnector connector;

    public MyKafkaConsumer(String topic) {
        this.topic = topic;
        Properties properties = new Properties();
        properties.put("bootstrap.servers", KafkaProperties.BROKER_LIST);
        properties.put("zookeeper.connect", KafkaProperties.ZK);
        properties.put("group.id", "g1");
        properties.put("auto.offset.reset", "latest");     //从最早的消息开始读取
        connector = Consumer.createJavaConsumerConnector(new ConsumerConfig(properties));
    }

    @Override
    public void run() {

        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put("spark-kafka", 1);
        Map<String, List<KafkaStream<String, String>>> map =
                connector.createMessageStreams(topicCountMap, new StringDecoder(null), new StringDecoder(null));
        map.get(topic).forEach(kafkaStream -> {

            ConsumerIterator<String, String> iterator = kafkaStream.iterator();
            while (iterator.hasNext()) {
                MessageAndMetadata<String, String> messageAndMetadata = iterator.next();
                System.out.println(messageAndMetadata.key() + ":" + messageAndMetadata.message());
            }
        });
    }
}
