package mr;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/1 21:21
 */
public class MRApp {

//    static class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
//
//
//        @Override
//        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
//
//            System.out.println(key.get());
//            String[] line = value.toString().split(" ");
//            Arrays.stream(line).forEach(s -> {
//                System.out.println("word:" + s);
//                Text word = new Text(s);
//                try {
//                    context.write(word, new IntWritable(1));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//    }

//    static class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
//
//        @Override
//        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
//            System.out.println(Thread.currentThread().getName());
//            int count = 0;
//            for (IntWritable intWritable : values) {
//                count += intWritable.get();
//            }
//            context.write(key, new IntWritable(count));
//        }
//    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        Configuration configuration = new Configuration();
//        configuration.set("mapreduce.app-submission.cross-platform","true");
//        configuration.set("fs.defaultFS", "file:///");
        configuration.set("fs.defaultFS", "hdfs://node00:8020/");
        Job job = Job.getInstance(configuration);
        job.setJobName("word count program");
        job.setJarByClass(MRApp.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setMapperClass(WordCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setReducerClass(WordCountReducer.class);
        job.setNumReduceTasks(1);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);


        //设置切片数目
//        FileInputFormat.setMinInputSplitSize(job, 1);
//        FileInputFormat.setMaxInputSplitSize(job, 20);


        //设置分区
//        job.setPartitionerClass(MyPartitioner.class);

        //设置combiner
//        job.setCombinerClass(WordCountReducer.class);


        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
