package mr.chain;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.chain.ChainReducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/1 21:21
 */
public class MRChainApp {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "file:///");
        Job job = Job.getInstance(configuration);
        job.setJobName("chain program");
        job.setJarByClass(MRChainApp.class);

        FileSystem fileSystem = FileSystem.newInstance(configuration);
        if (fileSystem.exists(new Path("d:/mr/out/chain"))) {
            fileSystem.delete(new Path("d:/mr/out/chain"), true);
        }

        job.setInputFormatClass(TextInputFormat.class);

        //链式处理需要按照顺序执行
        //Mapper链添加WCMapper1
        ChainMapper.addMapper(job, WCMapper1.class, LongWritable.class, Text.class, Text.class, IntWritable.class, configuration);
        //Mapper链添加WCMapper2
        ChainMapper.addMapper(job, WCMapper2.class, Text.class, IntWritable.class, Text.class, IntWritable.class, configuration);
        //Reducer链添加WCReducer
        ChainReducer.setReducer(job, WCReducer.class, Text.class, IntWritable.class, Text.class, IntWritable.class, configuration);
        //Reducer链添加WCReduceMapper
        ChainReducer.addMapper(job, WCReduceMapper.class, Text.class, IntWritable.class, Text.class, IntWritable.class, configuration);

        FileInputFormat.addInputPath(job, new Path("d:\\mr\\chain.txt"));
        FileOutputFormat.setOutputPath(job, new Path("d:\\mr\\out\\chain"));
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
