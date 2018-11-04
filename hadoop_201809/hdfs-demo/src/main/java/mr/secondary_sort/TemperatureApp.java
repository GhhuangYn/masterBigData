package mr.secondary_sort;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.InputSampler;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;

import java.io.IOException;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/1 21:21
 */
public class TemperatureApp {

    static class TemperatureMapper extends Mapper<LongWritable, Text, Combokey, NullWritable> {

        //使用combokey
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            //2017 20
            String[] line = value.toString().split(" ");
            Combokey combokey = new Combokey();
            combokey.setYear(Integer.parseInt(line[0]));
            combokey.setTemperature(Integer.parseInt(line[1]));
            context.write(combokey, NullWritable.get());

        }
    }

    static class TemperatureReducer extends Reducer<Combokey, NullWritable, IntWritable, IntWritable> {

        @Override
        public void reduce(Combokey key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {

            System.out.println("============reduce============");
            for (NullWritable nullWritable : values) {
                System.out.println(key.getYear() + ":" + key.getTemperature());
            }
            context.write(new IntWritable(key.getYear()), new IntWritable(key.getTemperature()));
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        Configuration configuration = new Configuration();


        configuration.set("fs.defaultFS", "file:///");
        Job job = Job.getInstance(configuration);
        job.setJobName("secondary sort program");
        job.setJarByClass(TemperatureApp.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setPartitionerClass(YearPartitioner.class);

        Path inputPath = new Path(args[0]);
        Path outputPath = new Path(args[1]);

        FileSystem fileSystem = FileSystem.newInstance(configuration);
        if (fileSystem.exists(outputPath)) {
            fileSystem.delete(outputPath, true);
        }

        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);

        job.setMapperClass(TemperatureMapper.class);
        job.setMapOutputKeyClass(Combokey.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.setReducerClass(TemperatureReducer.class);
        job.setNumReduceTasks(3);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(IntWritable.class);

        //================二次排序=================

        job.setSortComparatorClass(CombokeyComparator.class);       //设置排序对比器
        job.setGroupingComparatorClass(GroupComparator.class);      //组比较器

        //================二次排序 End=================

        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
