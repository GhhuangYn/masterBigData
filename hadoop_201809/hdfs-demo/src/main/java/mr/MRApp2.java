package mr;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import java.io.IOException;
import java.util.Arrays;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/1 21:21
 */
public class MRApp2 {

    static class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {


        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            System.out.println(key.get());
            String[] line = value.toString().split(" ");
            Arrays.stream(line).forEach(s -> {
                System.out.println("word:" + s);
                Text word = new Text(s);
                try {
                    context.write(word, new IntWritable(1));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    static class WordCountMapperToSeq extends Mapper<IntWritable, Text, Text, IntWritable> {


        @Override
        protected void map(IntWritable key, Text value, Context context) throws IOException, InterruptedException {

            System.out.println(key.get());
            String[] line = value.toString().split(" ");
            Arrays.stream(line).forEach(s -> {
                System.out.println("word:" + s);
                Text word = new Text(s);
                try {
                    context.write(word, new IntWritable(1));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
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
        job.setJarByClass(MRApp2.class);


        job.setMapperClass(WordCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setReducerClass(WordCountReducer.class);
        job.setNumReduceTasks(1);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        Path inputPath = new Path(args[0]);
        Path outputPath = new Path(args[1]);

        //多输入问题
        MultipleInputs.addInputPath(job,new Path("file:///d:/mr/3.txt"),TextInputFormat.class,WordCountMapper.class);
        MultipleInputs.addInputPath(job,new Path("file:///d:/mr/a.seq"),SequenceFileInputFormat.class,WordCountMapperToSeq.class);

//        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
