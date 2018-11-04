package mr.myhive;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/8 13:27
 */
public class CustomerOrderPartitioner extends Partitioner<CustomerOrderKey, NullWritable> {

    //按照cid进行分区
    @Override
    public int getPartition(CustomerOrderKey customerOrderKey, NullWritable nullWritable, int numPartitions) {

        return customerOrderKey.getCid() % numPartitions;
    }
}
