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
 * ����2����ǰһ������Ĺ�����ݽ���ͳ�ƣ���Ҫ������¹���
 ͳ�����ȣ�����ͳ��
 ͳ��Ƶ�ʣ�ÿ��ͳ��ǰһ�������
 ͳ��ָ�꣺�ع���pv�������click�������click_ratio
 ͳ��ά�ȣ�����area_id
 */
public class MrPvClickByAreaDayApp {
    static class PvClickMapper extends Mapper<LongWritable,Text,Text,AdMetricBean>{
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            //�����line�ַ����ֶθ�ʽarea_id user_id visit_type date���ָ�����Tab��
            String[] fields = line.split("\t");
            //����ID
            String area_id = fields[0];
            //�������view_type,1��ʾ�ع⣬2��ʾ���
            int view_type = Integer.parseInt(fields[2]);
            //����
            String date = fields[3];
            int pv = 0;
            int click = 0;
            if(view_type == 1){
                pv = 1;
            }else if(view_type == 2){
                click = 1;
            }
            //��ϼ�
            String keyStr = area_id + "-" + date;
            context.write(new Text(keyStr),new AdMetricBean(pv,click));
        }
    }
    static class PvClickReducer extends Reducer<Text,AdMetricBean,Text,NullWritable>{
        //reduce��������������һ�����ݣ�һ��key����������value���һ�飨k:v1,v2,v3��
        @Override
        protected void reduce(Text key, Iterable<AdMetricBean> values, Context context) throws IOException, InterruptedException {
            //����һ��������
            long pv = 0;
            long click = 0;
            //����һ�����ݣ���key���ִ����ۼӵ�count
            for(AdMetricBean amb : values){
                pv += amb.getPv();
                click += amb.getClick();
            }
            double clickRatio = (double)click / pv * 100;
            //������λС��
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
        //������ҵ����
        job.setJobName("MrPvClickByAreaDayApp");
        //��������
        job.setJarByClass(MrPvClickByAreaDayApp.class);
        //������ҵ��ʹ�õ�Mapper��Reducer��
        job.setMapperClass(PvClickMapper.class);
        job.setReducerClass(PvClickReducer.class);
        //����Mapper�׶ε����key���ͺ�value����
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(AdMetricBean.class);
        //����reducer�׶ε����key���ͺ�value����
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);
        //����job������·�������·��
        FileInputFormat.setInputPaths(job,new Path(inputPath));
        FileOutputFormat.setOutputPath(job,new Path(outputPath));
        System.exit(job.waitForCompletion(true)?0:1);
    }

}
