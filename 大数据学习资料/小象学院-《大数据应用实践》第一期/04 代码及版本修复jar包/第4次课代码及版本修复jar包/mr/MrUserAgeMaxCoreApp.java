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
 * Created by xiaoguanyu on 2017/12/29.
 * 找出不同性别的不同年龄段用户对某个产品的最高打分，需要完成如下功能
 日志数据格式
 姓名（name），字符串类型
 年龄（age），整数类型
 性别（gender），字符串类型
 打分（core），整数类型，0到100的整数值
 统计指标
 指标：最高分
 维度：姓名，性别，年龄
 */
public class MrUserAgeMaxCoreApp {
    static class UserAgeMaxCoreMapper extends Mapper<LongWritable,Text,Text,Text>{

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            //拿到一行数据，将输入的序列化数据转换成字符串
            String line = value.toString();
            //将一行数据按照分隔符拆分,字段格式name age gender score
            String[] fields = line.split("\t");
            String gender = fields[2];
            String nameAgeScore = fields[0] + "\t" + fields[1] + "\t" + fields[3];
            context.write(new Text(gender),new Text(nameAgeScore));
        }
    }

    static class UserAgeMaxCoreReducer extends Reducer<Text,Text,Text,Text>{

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            int maxCore = 0;
            String name = "";
            String age = "";
            String gender = "";
            for(Text val : values){
                String nameAgeScore = val.toString();
                String[] fields = nameAgeScore.split("\t");
                int score = Integer.parseInt(fields[2]);
                if(score > maxCore){
                    name = fields[0];
                    age = fields[1];
                    gender = key.toString();
                    maxCore = score;
                }
            }
            context.write(new Text(name),new Text(age + "\t" + gender + "\t" + maxCore));

        }
    }
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        String inputPath = args[0];
        String outputPath = args[1];
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf);
        //设置作业名称
        job.setJobName("UserAgeMaxCoreApp");
        //设置主类
        job.setJarByClass(MrUserAgeMaxCoreApp.class);
        //设置作业中使用的Mapper和Reducer类
        job.setMapperClass(UserAgeMaxCoreMapper.class);
        job.setReducerClass(UserAgeMaxCoreReducer.class);
        //设置自定义partitioner
        job.setPartitionerClass(AgePartitioner.class);
        job.setNumReduceTasks(3);
        //设置Mapper阶段的输出key类型和value类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        //设置reducer阶段的输出key类型和value类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        //设置job的输入路径和输出路径
        FileInputFormat.setInputPaths(job,new Path(inputPath));
        FileOutputFormat.setOutputPath(job,new Path(outputPath));
        System.exit(job.waitForCompletion(true)?0:1);
    }

}
