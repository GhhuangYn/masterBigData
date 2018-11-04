package mr.max_temp;

import javafx.scene.control.TextFormatter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.InputSampler;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;

import java.io.IOException;
import java.util.Arrays;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/1 21:21
 */
public class TemperatureApp {

    static class TemperatureMapper extends Mapper<IntWritable, IntWritable, IntWritable, IntWritable> {

        //使用seqFile作为输入文件
        @Override
        protected void map(IntWritable key, IntWritable value, Context context) throws IOException, InterruptedException {

            //2017 20
            context.write(key, value);
        }
    }

    static class TemperatureReducer extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {

        @Override
        public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

            int max = 0;
            for (IntWritable intWritable : values) {
                int tmp = intWritable.get();
                if (max < tmp) {
                    max = tmp;
                }
            }
            context.write(key, new IntWritable(max));

        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        Configuration configuration = new Configuration();


        configuration.set("fs.defaultFS", "file:///");
        Job job = Job.getInstance(configuration);
        job.setJobName("word count program");
        job.setJarByClass(TemperatureApp.class);

        Path inputPath = new Path(args[0]);
        Path outputPath = new Path(args[1]);

        FileSystem fileSystem = FileSystem.newInstance(configuration);
        if (fileSystem.exists(outputPath)) {
            fileSystem.delete(outputPath, true);
        }

        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);

        //输出格式采用序列化文件
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setMapperClass(TemperatureMapper.class);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setReducerClass(TemperatureReducer.class);
        job.setNumReduceTasks(3);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(IntWritable.class);

        //================全排序=================
        //设置全排序分区类
        job.setPartitionerClass(TotalOrderPartitioner.class);
        //设置采样器
        InputSampler.Sampler<IntWritable, IntWritable> sampler =
                new InputSampler.RandomSampler<>(0.1, 10000, 10);
        InputSampler.writePartitionFile(job, sampler);

        //设置分区文件
        TotalOrderPartitioner.setPartitionFile(job.getConfiguration(), new Path("file:///d:/mr/temp_part.lst"));
        //================全排序 End=================

        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
