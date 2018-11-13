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
            //�õ�һ�����ݣ�����������л�����ת�����ַ���
            String line = value.toString();
            //��һ�����ݰ��շָ������
            String[] words = line.split("\t");
            //�����������ݣ��������<k,1>
            for(String word:words){
                //��Ҫ���л�д��
                context.write(new Text(word),new IntWritable(1));
            }
        }
    }
    static class WordCountReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
        //reduce��������������һ�����ݣ�һ��key����������value���һ�飨k:v1,v2,v3��
        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            //����һ��������
            int count = 0;
            //����һ�����ݣ���key���ִ����ۼӵ�count
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
        //������ҵ����
        job.setJobName(jobName);
        //��������
        job.setJarByClass(WordCountApp.class);
        //������ҵ��ʹ�õ�Mapper��Reducer��
        job.setMapperClass(WordCountMapper.class);
        job.setReducerClass(WordCountReducer.class);
        //����Mapper�׶ε����key���ͺ�value����
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        //����reducer�׶ε����key���ͺ�value����
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        //����job������·�������·��
        FileInputFormat.setInputPaths(job,new Path(inputPath));
        FileOutputFormat.setOutputPath(job,new Path(outputPath));
        System.exit(job.waitForCompletion(true)?0:1);
    }

}
