package imooc.kafka;

import org.junit.Test;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/31 10:22
 */
public class KafkaTest {

    @Test
    public void testProducer() {
        MyKafkaProducer kafkaProducer = new MyKafkaProducer("spark-kafka");
        kafkaProducer.run();
    }

    @Test
    public void testConsumer() {
        MyKafkaConsumer kafkaConsumer = new MyKafkaConsumer("spark-kafka");
        kafkaConsumer.run();
    }

}
