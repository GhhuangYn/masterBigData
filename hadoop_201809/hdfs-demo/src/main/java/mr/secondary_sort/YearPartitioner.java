package mr.secondary_sort;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * @Description: partitioner 接收的时map的输出结果，进行分区
 * @author: HuangYn
 * @date: 2018/9/5 12:14
 */
public class YearPartitioner extends Partitioner<Combokey, NullWritable> {

    @Override
    public int getPartition(Combokey combokey, NullWritable nullWritable, int numPartitions) {
        System.out.println("======= YearPartitioner ======");
        //按照年份进行分组
        return combokey.getYear() % numPartitions;
    }
}
