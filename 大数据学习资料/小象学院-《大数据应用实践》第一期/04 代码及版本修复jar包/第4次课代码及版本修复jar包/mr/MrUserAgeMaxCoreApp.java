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
 * �ҳ���ͬ�Ա�Ĳ�ͬ������û���ĳ����Ʒ����ߴ�֣���Ҫ������¹���
 ��־���ݸ�ʽ
 ������name�����ַ�������
 ���䣨age������������
 �Ա�gender�����ַ�������
 ��֣�core�����������ͣ�0��100������ֵ
 ͳ��ָ��
 ָ�꣺��߷�
 ά�ȣ��������Ա�����
 */
public class MrUserAgeMaxCoreApp {
    static class UserAgeMaxCoreMapper extends Mapper<LongWritable,Text,Text,Text>{

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            //�õ�һ�����ݣ�����������л�����ת�����ַ���
            String line = value.toString();
            //��һ�����ݰ��շָ������,�ֶθ�ʽname age gender score
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
        //������ҵ����
        job.setJobName("UserAgeMaxCoreApp");
        //��������
        job.setJarByClass(MrUserAgeMaxCoreApp.class);
        //������ҵ��ʹ�õ�Mapper��Reducer��
        job.setMapperClass(UserAgeMaxCoreMapper.class);
        job.setReducerClass(UserAgeMaxCoreReducer.class);
        //�����Զ���partitioner
        job.setPartitionerClass(AgePartitioner.class);
        job.setNumReduceTasks(3);
        //����Mapper�׶ε����key���ͺ�value����
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        //����reducer�׶ε����key���ͺ�value����
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        //����job������·�������·��
        FileInputFormat.setInputPaths(job,new Path(inputPath));
        FileOutputFormat.setOutputPath(job,new Path(outputPath));
        System.exit(job.waitForCompletion(true)?0:1);
    }

}
