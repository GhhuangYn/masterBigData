package mr.max_temp;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/5 12:14
 */
public class YearPartitioner extends Partitioner<Text, IntWritable> {

    @Override
    public int getPartition(Text year, IntWritable temperature, int numPartitions) {

      /*  int width = Integer.parseInt(year.toString());
        if (width > 2005) {
            return 0;
        } else if (width > 2000) {
            return 1;
        } else {
            return 2;
        }*/
        return 0;
    }
}
