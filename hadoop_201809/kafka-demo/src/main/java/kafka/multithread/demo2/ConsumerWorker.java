package kafka.multithread.demo2;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.List;
import java.util.Map;

/**
 * @Description: 真正执行消费逻辑并上报位移信息给ConsumerThreadHandler
 * @author: HuangYn
 * @date: 2018/10/4 14:39
 */
public class ConsumerWorker<K, V> implements Runnable {

    private final ConsumerRecords<K, V> records;
    private final Map<TopicPartition, OffsetAndMetadata> offsets;

    public ConsumerWorker(ConsumerRecords<K, V> records, Map<TopicPartition, OffsetAndMetadata> offsets) {
        this.records = records;
        this.offsets = offsets;
    }

    @Override
    public void run() {

        //遍历每个分区
        records.partitions().forEach(p -> {

            //获取每个分区的所有记录
            List<ConsumerRecord<K, V>> partitionRecords = records.records(p);
            //遍历分区记录
            partitionRecords.forEach(pr -> {
                //插入消息处理逻辑
                System.out.println(String.format("topic=%s,partition=%d,offset=%d",
                        pr.topic(), pr.partition(), pr.offset()));
            });
            //上报位移信息
            long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();       //获取最后一个记录的位置

            //更新分区与位移的映射关系
            synchronized (offsets) {
                if (!offsets.containsKey(p)) {
                    offsets.put(p, new OffsetAndMetadata(lastOffset + 1));
                } else {
                    long cur = offsets.get(p).offset();
                    if (cur <= lastOffset + 1)
                        offsets.put(p, new OffsetAndMetadata(lastOffset + 1));
                }
            }
        });

    }
}
