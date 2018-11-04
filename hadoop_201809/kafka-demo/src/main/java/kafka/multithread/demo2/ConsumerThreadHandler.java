package kafka.multithread.demo2;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 多线程管理类，用于创建线程池以及为每个线程分配消息集合。另外完成位移提交
 * @author: HuangYn
 * @date: 2018/10/4 14:14
 */
public class ConsumerThreadHandler<K, V> {

    private final KafkaConsumer<K, V> kafkaConsumer;   //使用全局的consumer
    private ExecutorService executorService;
    private final Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();     //存放分区-位移信息

    public ConsumerThreadHandler(String brokerList, String groupId, String topic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerList);
        props.put("group.id", groupId);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("enable.auto.commit", "false");
        props.put("auto.offset.reset", "earliest");
        this.kafkaConsumer = new KafkaConsumer<K, V>(props);
        this.kafkaConsumer.subscribe(Arrays.asList(topic), new ConsumerRebalanceListener() {

            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                //提交位移
                System.out.println("提交位移");
                kafkaConsumer.commitSync(offsets);
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                offsets.clear();
            }
        });
    }

    /**
     * 消费方法
     *
     * @param threadNum
     */
    public void consume(int threadNum) {
        executorService = new ThreadPoolExecutor(
                threadNum,
                threadNum,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy());
        try {
            while (true) {
                ConsumerRecords<K, V> records = kafkaConsumer.poll(1000);
                if (!records.isEmpty()) {
                    //如果获取到数据，放入工作线程
                    executorService.submit(new ConsumerWorker<>(records, offsets));
                }
                commitOffsets();
            }
        } catch (WakeupException e) {

        } finally {
            commitOffsets();
            kafkaConsumer.close();
        }
    }

    /**
     * 提交位移
     */
    private void commitOffsets() {
        //尽量降低synchronized块对offsets的锁定时间
        Map<TopicPartition, OffsetAndMetadata> unmodifiedMap;
        synchronized (offsets) {
            if (offsets.isEmpty()) {
                return;
            }
            unmodifiedMap = Collections.unmodifiableMap(new HashMap<>(offsets));
            offsets.clear();
        }
        kafkaConsumer.commitSync(unmodifiedMap);
    }

    public void close() {
        kafkaConsumer.wakeup();
        executorService.shutdown();
    }
}
