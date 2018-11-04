package imooc.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/31 10:01
 */
public class MyKafkaProducer extends Thread {

    private String topic;
    private KafkaProducer<String, String> kafkaProducer;

    public MyKafkaProducer(String topic) {
        this.topic = topic;
        Properties props = new Properties();
        props.put("bootstrap.servers", KafkaProperties.BROKER_LIST);
        props.put("key.serializer", KafkaProperties.STRING_SERIALIZER);
        props.put("value.serializer", KafkaProperties.STRING_SERIALIZER);
        props.put("acks", "-1");        //不需要确保消息写入
        kafkaProducer = new KafkaProducer<>(props);
    }

    @Override
    public void run() {
        int i = 0;
        while (true) {

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, "msg-" + i, "hello " + i);
            kafkaProducer.send(record, (recordMetadata, ex) -> {
                if (ex != null) {
                    ex.printStackTrace();
                } else {
                    System.out.println("offset:" + recordMetadata.offset() + ",partition:" + recordMetadata.partition());
                }
            });
            i++;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
