package mr.myhive;

import mr.secondary_sort.Combokey;
import mr.secondary_sort.CombokeyComparator;
import mr.secondary_sort.GroupComparator;
import mr.secondary_sort.YearPartitioner;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/1 21:21
 */
public class JoinApp {

    static class JoinMapper extends Mapper<LongWritable, Text, CustomerOrderKey, NullWritable> {

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            FileSplit fileSplit = (FileSplit) context.getInputSplit();
            Path inputPath = fileSplit.getPath();
            CustomerOrderKey cokey = new CustomerOrderKey();

            String line = value.toString();
            if (inputPath.toString().contains("customer")) {
                int cid = Integer.parseInt(line.substring(0, line.indexOf(",")));
                cokey.setType(0);
                cokey.setCid(cid);
                cokey.setCustomerInfo(line.substring(line.indexOf(",") + 1));
            } else {
                cokey.setType(1);
                int cid = Integer.parseInt(line.substring(line.lastIndexOf(",") + 1));
                cokey.setCid(cid);
                cokey.setOrderInfo(line.substring(0, line.lastIndexOf(",")));
            }
            context.write(cokey, NullWritable.get());
        }
    }

    static class JoinReducer extends Reducer<CustomerOrderKey, NullWritable, Text, NullWritable> {

        @Override
        public void reduce(CustomerOrderKey key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            System.out.println("===========reduce===========");

            String customerInfo = "";
            String orderInfo = "";
            values.iterator().next();
            customerInfo = key.getCid() + "," + key.getCustomerInfo();
            System.out.println(customerInfo);
            if (!values.iterator().hasNext()){
                context.write(new Text(customerInfo + ",NULL"), NullWritable.get());
            }
            for (NullWritable nullWritable : values) {
                System.out.println("----");
                System.out.println(key);
                orderInfo = key.getOrderInfo();
                context.write(new Text(customerInfo + "," + orderInfo), NullWritable.get());
            }
        }

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        Configuration configuration = new Configuration();


        configuration.set("fs.defaultFS", "file:///");
        Job job = Job.getInstance(configuration);
        job.setJobName("secondary sort program");
        job.setJarByClass(JoinApp.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setPartitionerClass(YearPartitioner.class);

        Path inputPath1 = new Path("file:///d:/mr/orders.txt");
        Path inputPath2 = new Path("file:///d:/mr/customers.txt");
        Path outputPath = new Path("file:///d:/mr/out/hive");

        FileSystem fileSystem = FileSystem.newInstance(configuration);
        if (fileSystem.exists(outputPath)) {
            fileSystem.delete(outputPath, true);
        }

        FileInputFormat.addInputPath(job, inputPath1);
        FileInputFormat.addInputPath(job, inputPath2);
        FileOutputFormat.setOutputPath(job, outputPath);

        job.setMapperClass(JoinMapper.class);
        job.setMapOutputKeyClass(CustomerOrderKey.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.setReducerClass(JoinReducer.class);
        job.setNumReduceTasks(3);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        //================二次排序=================

        job.setPartitionerClass(CustomerOrderPartitioner.class);
        job.setSortComparatorClass(CustomerOrderKeyComparator.class);       //设置排序对比器
        job.setGroupingComparatorClass(CustomerOrderGroupComparator.class);      //组比较器

        //================二次排序 End=================

        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
