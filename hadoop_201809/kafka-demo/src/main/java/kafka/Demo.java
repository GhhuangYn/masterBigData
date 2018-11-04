package kafka;

import org.apache.hadoop.hdfs.util.ByteArray;
import org.apache.kafka.clients.Metadata;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/29 14:58
 */
public class Demo {

    @Test
    public void testProducer() throws InterruptedException, ExecutionException {

        Properties props = new Properties();

        props.put("bootstrap.servers", "node00:9092");    //required
        props.put("zookeeper.connect", "node00:2181");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");  //required
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer"); //required
        props.put("acks", "-1");        //acks制定在给producer发送响应前，leader broker需要确保已成功写入的副本数
        props.put("retires", "3");
        props.put("batch.size", "323840");
        props.put("linger.ms", "10");
        props.put("buffer.memory", "33554432");
        props.put("max.block.ms", "3000");

        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(500);
            ProducerRecord<String, String> record = new ProducerRecord<>("test1", "msg-" + i, "hi leo-" + i);

            //异步发送
            producer.send(record, (metadata, error) -> {
                System.out.println("已发送。。");
                System.out.println("partition: " + metadata.partition());
                System.out.println("offset: " + metadata.offset());
                System.out.println("date: " + new Date(metadata.timestamp()));
            });

            //同步发送
//            RecordMetadata metadata = producer.send(record).get();
//            System.out.println("已发送。。");
//            System.out.println("partition: " + metadata.partition());
//            System.out.println("offset: " + metadata.offset());
//            System.out.println("date: " + new Date(metadata.timestamp()));

        }
        System.out.println("over..");
        producer.close();
    }

    @Test
    public void testPartitioner() throws InterruptedException {

        Properties props = new Properties();

        props.put("bootstrap.servers", "node00:9092");    //required
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");  //required
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer"); //required
        props.put("linger.ms", "10");
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "kafka.MyPartitioner");      //自定义分区

        Producer<String, String> producer = new KafkaProducer<>(props);
        Random random = new Random();
        String key = "";
        for (int i = 0; i < 10; i++) {
            if (random.nextInt(100) % 2 == 0) {
                key = "audit";
            } else {
                key = "msg-" + i;
            }
            ProducerRecord<String, String> record = new ProducerRecord<>("test1", key, "hi leo-" + i);

            producer.send(record, (metadata, error) -> {
                System.out.println("已发送消息..");
                if (error == null) {
                    System.out.println("partition: " + metadata.partition());
                    System.out.println("offset: " + metadata.offset());
                    System.out.println("key: " + record.key());
                } else {
                    System.out.println(error.getMessage());
                    System.out.println(error.getCause().getMessage());
                }

            });
            Thread.sleep(500);

        }
        System.out.println("over..");
        producer.close();
    }

    @Test
    public void testInterceptor() throws InterruptedException, ExecutionException {

        Properties props = new Properties();

        props.put("bootstrap.servers", "node00:9092");    //required
        props.put("zookeeper.connect", "node00:2181");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");  //required
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer"); //required
        props.put("acks", "-1");        //acks制定在给producer发送响应前，leader broker需要确保已成功写入的副本数
        props.put("retires", "3");
        props.put("batch.size", "323840");
        props.put("linger.ms", "10");
        props.put("buffer.memory", "33554432");
        props.put("max.block.ms", "3000");

        //设置拦截器
        //构建拦截器链
        List<String> interceptors = Arrays.asList("kafka.TimestampInterceptor", "kafka.CounterInterceptor");
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, interceptors);

        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 20; i++) {
            Thread.sleep(500);
            ProducerRecord<String, String> record = new ProducerRecord<>("test1", "msg-" + i, "hi leo-" + i);

            //异步发送
            producer.send(record, (metadata, error) -> {
                System.out.println("已发送。。");
                System.out.println("partition: " + metadata.partition());
                System.out.println("offset: " + metadata.offset());
            });


        }
        System.out.println("over..");
        producer.close();
    }

    @Test
    public void testConsumer() {

        String topic = "test1";
        String group = "g1";

        Properties props = new Properties();
        props.put("bootstrap.servers", "node00:9092");   //required
        props.put("group.id", group);   //required
        props.put("zookeeper.connect", "node00:2181");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "latest");     //从最早的消息开始读取
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");  //required
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"); //required
        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));       //订阅topic
        try {
            while (true) {
                ConsumerRecords<String, String> record = consumer.poll(1000);    //一次poll()可以拿到很多数据，不足1s时会阻塞,1000ms是最大阻塞时间
                record.iterator().forEachRemaining(r -> {
                    System.out.printf("offset=%d, key=%s, value=%s, partition=%d\n", r.offset(), r.key(), r.value(), r.partition());
                });
            }
        } finally {
            consumer.close();
        }

    }

    @Test
    public void testConsumer3() {

        String topic = "test1";
        String group = "g1";

        Properties props = new Properties();
        props.put("bootstrap.servers", "node00:9092");   //required
        props.put("group.id", group);   //required
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "earliest");     //从最早的消息开始读取
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");  //required
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"); //required
        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));       //订阅topic
        try {
            while (true) {
                ConsumerRecords<String, String> record = consumer.poll(1000);
                record.iterator().forEachRemaining(r -> {
                    System.out.printf("offset=%d, key=%s, value=%s, partition=%d\n", r.offset(), r.key(), r.value(), r.partition());
                });
            }
        } finally {
            consumer.close();
        }

    }

    @Test
    public void testConsumer2() {

        String topic = "test1";
        String group = "g1";

        Properties props = new Properties();
        props.put("bootstrap.servers", "node00:9092");   //required
        props.put("group.id", group);   //required
        props.put("zookeeper.connect", "node00:2181");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "earliest");     //从最早的消息开始读取
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");  //required
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer"); //required

        Consumer<String, ByteArray> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));       //订阅topic
        try {
            while (true) {
                ConsumerRecords<String, org.apache.hadoop.hdfs.util.ByteArray> record = consumer.poll(1000);
                System.out.println(record.count());
                record.iterator().forEachRemaining(r -> {
                    System.out.printf("offset=%d, key=%s, value=%s\n", r.offset(), r.key(),
                            r.value());
                });
            }
        } finally {
            consumer.close();
        }
    }

    //rebalance监听器
    @Test
    public void testRebalance() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "node00:9092");
        props.put("group.id", "g1");
//        props.put("enable.auto.commit","false");        //关闭自动提交位移
        props.put("auto.offset.reset", "earliest");
        final KafkaConsumer<String, String> consumer =
                new KafkaConsumer(props, new StringDeserializer(), new StringDeserializer());
        final AtomicLong totoalRebalanceTimeMs = new AtomicLong(0L);    //总的rebalance时长
        final AtomicLong joinStart = new AtomicLong(0L);                //每一次rebalance的开始时间

        consumer.subscribe(Arrays.asList("test1"), new ConsumerRebalanceListener() {        //rebalance监听器

            //在coordinator开启新一轮rebalance前调用
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                //这里可以进行一些操作，比如把手动提交的位移存储到第三方
                partitions.forEach(p -> System.out.println("position:"+consumer.position(p)));
                joinStart.set(System.currentTimeMillis());
            }

            //在rebalance完成后调用
            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                totoalRebalanceTimeMs.addAndGet(System.currentTimeMillis() - joinStart.get());        //更新总的rebalance时长
                partitions.forEach(p -> System.out.println("partition: "+p.partition()));
            }
        });

        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = consumer.poll(1000);
                consumerRecords.forEach(r ->
                        System.out.printf("offset=%d, key=%s, value=%s, partition=%d\n", r.offset(), r.key(), r.value(), r.partition()));

            }

        } finally {
            System.out.println("totoalRebalanceTimeMs: " + totoalRebalanceTimeMs);
            consumer.close();
        }
    }

}
