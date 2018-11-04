package mr;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/4 21:02
 */
public class MyPartitioner extends Partitioner<Text, IntWritable> {

    @Override
    public int getPartition(Text text, IntWritable intWritable, int numPartitions) {

        System.out.println("=============================");

        return 0;
    }
}
