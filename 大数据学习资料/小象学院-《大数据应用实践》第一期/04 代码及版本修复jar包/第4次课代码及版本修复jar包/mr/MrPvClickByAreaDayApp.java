package bigdata.mr;

import bigdata.mr.bean.AdMetricBean;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Created by xiaoguanyu on 2017/12/28.
 * 需求2：对前一天产生的广告数据进行统计，需要完成如下功能
 统计粒度：按天统计
 统计频率：每天统计前一天的数据
 统计指标：曝光量pv，点击量click，点击率click_ratio
 统计维度：地域area_id
 */
public class MrPvClickByAreaDayApp {
    static class PvClickMapper extends Mapper<LongWritable,Text,Text,AdMetricBean>{
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            //输入的line字符串字段格式area_id user_id visit_type date，分隔符是Tab键
            String[] fields = line.split("\t");
            //地域ID
            String area_id = fields[0];
            //浏览类型view_type,1表示曝光，2表示点击
            int view_type = Integer.parseInt(fields[2]);
            //日期
            String date = fields[3];
            int pv = 0;
            int click = 0;
            if(view_type == 1){
                pv = 1;
            }else if(view_type == 2){
                click = 1;
            }
            //组合键
            String keyStr = area_id + "-" + date;
            context.write(new Text(keyStr),new AdMetricBean(pv,click));
        }
    }
    static class PvClickReducer extends Reducer<Text,AdMetricBean,Text,NullWritable>{
        //reduce方法是针对输入的一组数据，一个key和它的所有value组成一组（k:v1,v2,v3）
        @Override
        protected void reduce(Text key, Iterable<AdMetricBean> values, Context context) throws IOException, InterruptedException {
            //定义一个计数器
            long pv = 0;
            long click = 0;
            //遍历一组数据，将key出现次数累加到count
            for(AdMetricBean amb : values){
                pv += amb.getPv();
                click += amb.getClick();
            }
            double clickRatio = (double)click / pv * 100;
            //保留两位小数
            String clickRatioStr = String.format("%.2f", clickRatio).toString() + "%";
            String[] keys = key.toString().split("-");
            String line = keys[1] + "\t" + keys[0]+ "\t" + pv + "\t" + click + "\t" + clickRatioStr;

            context.write(new Text(line),NullWritable.get());

        }
    }
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        String inputPath = args[0];
        String outputPath = args[1];
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf);
        //设置作业名称
        job.setJobName("MrPvClickByAreaDayApp");
        //设置主类
        job.setJarByClass(MrPvClickByAreaDayApp.class);
        //设置作业中使用的Mapper和Reducer类
        job.setMapperClass(PvClickMapper.class);
        job.setReducerClass(PvClickReducer.class);
        //设置Mapper阶段的输出key类型和value类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(AdMetricBean.class);
        //设置reducer阶段的输出key类型和value类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);
        //设置job的输入路径和输出路径
        FileInputFormat.setInputPaths(job,new Path(inputPath));
        FileOutputFormat.setOutputPath(job,new Path(outputPath));
        System.exit(job.waitForCompletion(true)?0:1);
    }

}
