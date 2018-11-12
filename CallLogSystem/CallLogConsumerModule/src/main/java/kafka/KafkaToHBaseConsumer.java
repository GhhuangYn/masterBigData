package kafka;

import hbase.HBaseService;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/6 10:54
 */
public class KafkaToHBaseConsumer {

    private static HBaseService hBaseService = new HBaseService();

    public KafkaToHBaseConsumer() {
//        hBaseService = new HBaseService();
    }

    public static void main(String[] args) throws IOException {

        InputStream inputStream = ClassLoader.getSystemResourceAsStream("kafka.properties");
        Properties props = new Properties();
        //加载配置文件
        props.load(inputStream);

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(props);
        kafkaConsumer.subscribe(Arrays.asList(props.getProperty("kafka.topic")));
        try {
            while (true) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(500);
                records.forEach(record -> {
                    System.out.println(record.value());
                    hBaseService.save(record.value());
                });
            }

        } finally {
            kafkaConsumer.close();
        }
    }

}
