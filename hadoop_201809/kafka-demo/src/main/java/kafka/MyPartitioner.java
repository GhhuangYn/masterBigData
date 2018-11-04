package kafka;

import kafka.cluster.Partition;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/30 9:13
 */
public class MyPartitioner implements Partitioner {

    private Random random;

    //初始化资源
    @Override
    public void configure(Map<String, ?> configs) {
        random = new Random();
    }

    @Override
    public int partition(String topic, Object keyObj, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {

        String key = (String) keyObj;
        List<PartitionInfo> partitionInfoList = cluster.partitionsForTopic(topic);
        int partitionCount = partitionInfoList.size();
        if (key == null || key.isEmpty() || !key.contains("audit")) {
            return random.nextInt(partitionCount - 1);
        } else {
            return partitionCount - 1;
        }
    }

    @Override
    public void close() {

    }


}
