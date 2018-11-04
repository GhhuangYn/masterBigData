package mr.skew.stage2;

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
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/1 21:21
 */
public class MRApp {

    static class WordCountMapper extends Mapper<Text, Text, Text, IntWritable> {


        @Override
        protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {

            context.write(key, new IntWritable(Integer.parseInt(value.toString())));
        }
    }

    static class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int count = 0;
            for (IntWritable intWritable : values) {
                count += intWritable.get();
            }
            context.write(key, new IntWritable(count));
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "file:///");
        Job job = Job.getInstance(configuration);
        job.setJobName("word count program");
        job.setJarByClass(MRApp.class);

        FileSystem fileSystem = FileSystem.newInstance(configuration);
        if (fileSystem.exists(new Path("d:/mr/out/skew"))) {
            fileSystem.delete(new Path("d:/mr/out/skew"), true);
        }

        job.setInputFormatClass(KeyValueTextInputFormat.class);
        job.setMapperClass(WordCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setReducerClass(WordCountReducer.class);
        job.setNumReduceTasks(1);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        for (int i = 0; i < 4; i++) {
            FileInputFormat.addInputPath(job, new Path("d:\\mr\\out\\part-r-0000" + i));
        }
        FileOutputFormat.setOutputPath(job, new Path("d:\\mr\\out\\skew"));
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
