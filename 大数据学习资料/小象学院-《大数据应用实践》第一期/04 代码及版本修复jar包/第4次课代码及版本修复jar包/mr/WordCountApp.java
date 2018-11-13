package bigdata.mr;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Created by xiaoguanyu on 2017/12/26.
 */
public class WordCountApp {
    static class WordCountMapper extends Mapper<LongWritable,Text,Text,IntWritable>{

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            //拿到一行数据，将输入的序列化数据转换成字符串
            String line = value.toString();
            //将一行数据按照分隔符拆分
            String[] words = line.split("\t");
            //遍历单词数据，输出单词<k,1>
            for(String word:words){
                //需要序列化写出
                context.write(new Text(word),new IntWritable(1));
            }
        }
    }
    static class WordCountReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
        //reduce方法是针对输入的一组数据，一个key和它的所有value组成一组（k:v1,v2,v3）
        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            //定义一个计数器
            int count = 0;
            //遍历一组数据，将key出现次数累加到count
            for(IntWritable value : values){
                count += value.get();
            }
            context.write(key,new IntWritable(count));

        }
    }
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        String jobName = args[0];
        String inputPath = args[1];
        String outputPath = args[2];
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf);
        //设置作业名称
        job.setJobName(jobName);
        //设置主类
        job.setJarByClass(WordCountApp.class);
        //设置作业中使用的Mapper和Reducer类
        job.setMapperClass(WordCountMapper.class);
        job.setReducerClass(WordCountReducer.class);
        //设置Mapper阶段的输出key类型和value类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        //设置reducer阶段的输出key类型和value类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        //设置job的输入路径和输出路径
        FileInputFormat.setInputPaths(job,new Path(inputPath));
        FileOutputFormat.setOutputPath(job,new Path(outputPath));
        System.exit(job.waitForCompletion(true)?0:1);
    }

}
