package mr.chain;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/6 13:10
 */
public class WCReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        System.out.println("================");
        int count = 0;
        for (IntWritable intWritable : values) {
            count += intWritable.get();
        }
        context.write(key, new IntWritable(count));
    }
}
