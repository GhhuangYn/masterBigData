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
 * 需求1：一批TB或者PB量级的历史广告数据，需要完成如下功能
 统计粒度：按天统计
 统计指标：计算曝光量（PV）
 按照曝光量升序排列和倒序排列
 */
public class MrPvSortByDayApp{
    static class PvMapper extends Mapper<LongWritable,Text,Text,IntWritable>{
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            //输入的line字符串字段格式area_id user_id visit_type date，分隔符是Tab键
            String[] fields = line.split("\t");
            //浏览类型view_type,1表示曝光，2表示点击
            int view_type = Integer.parseInt(fields[2]);
            //日期
            String date = fields[3];
            int pv = 0;
            if(view_type == 1){
                pv = 1;
            }
            context.write(new Text(date),new IntWritable(pv));
        }
    }
    static class PvReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int sum_pv = 0;
            for(IntWritable pv : values){
                sum_pv += pv.get();
            }
            context.write(key,new IntWritable(sum_pv) );
        }
    }
    static class PvSortMapper extends Mapper<LongWritable,Text,IntWritable,Text>{
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            //输入的line字符串字段格式date pv
            String[] fields = line.split("\t");
            String date = fields[0];
            int pv = Integer.parseInt(fields[1]);
            context.write(new IntWritable(pv),new Text(date));
        }
    }
    static class PvSortReducer extends Reducer<IntWritable,Text,Text,IntWritable>{
        @Override
        protected void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for(Text dt : values){
                String date = dt.toString();
                context.write(new Text(date),key);
            }
        }
    }

    //重写IntWritable.Comparator比较器，默认返回正数，是升序，倒序返回负数
    public static class IntWritableDescComparator extends
            IntWritable.Comparator {
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return -super.compare(b1, s1, l1, b2, s2, l2);
        }
    }
    public static void main(String[] args) throws Exception {
        String inputPath = args[0];
        String outputPath = args[1];
        Path tmpPath = new Path("MrPvSortByDayTmp");
        Configuration conf = new Configuration();
        Job jobPv = Job.getInstance(conf,"MrPvByDay");
        //设置作业执行主类
        jobPv.setJarByClass(MrPvSortByDayApp.class);
        //设置作业中使用的mapper和reducer业务类
        jobPv.setMapperClass(PvMapper.class);
        jobPv.setReducerClass(PvReducer.class);
        //设置Mapper输出类型
        jobPv.setMapOutputKeyClass(Text.class);
        jobPv.setMapOutputValueClass(IntWritable.class);
        //设置Reducer输出类型
        jobPv.setOutputKeyClass(Text.class);
        jobPv.setOutputValueClass(IntWritable.class);
        //指定job输入数据路径
        FileInputFormat.setInputPaths(jobPv,new Path(inputPath));
        //指定job输出结果数据路径
        FileOutputFormat.setOutputPath(jobPv,tmpPath);

        boolean jobPvStatus = jobPv.waitForCompletion(true);

        Job jobPvSort = Job.getInstance(conf,"MrPvSortByDay");
        //设置作业执行主类
        jobPvSort.setJarByClass(MrPvSortByDayApp.class);
        //设置作业中使用的mapper和reducer业务类
        jobPvSort.setMapperClass(PvSortMapper.class);
        jobPvSort.setReducerClass(PvSortReducer.class);

        //在一个reduce任务中实现全局排序，设置reduce任务数为1，
        jobPvSort.setNumReduceTasks(1);

        //添加倒序排列比较器
        jobPvSort.setSortComparatorClass(IntWritableDescComparator.class);

        //设置Mapper输出类型
        jobPvSort.setMapOutputKeyClass(IntWritable.class);
        jobPvSort.setMapOutputValueClass(Text.class);
        //设置Reducer输出类型
        jobPvSort.setOutputKeyClass(Text.class);
        jobPvSort.setOutputValueClass(IntWritable.class);
        //指定job输入数据路径
        FileInputFormat.setInputPaths(jobPvSort,tmpPath);
        //指定job输出结果数据路径
        FileOutputFormat.setOutputPath(jobPvSort,new Path(outputPath));

        if(jobPvStatus){
            System.exit(jobPvSort.waitForCompletion(true)?0:1);
        }
    }
}
