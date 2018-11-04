package mr.skew.stage2;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

import java.util.Random;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/6 11:03
 */
public class RandomPartitioner extends Partitioner<Text, IntWritable> {

    @Override
    public int getPartition(Text text, IntWritable intWritable, int numPartitions) {
        return new Random().nextInt(numPartitions);
    }
}
