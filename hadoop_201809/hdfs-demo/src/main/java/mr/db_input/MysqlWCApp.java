package mr.db_input;

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
import org.apache.hadoop.mapreduce.lib.db.DBConfiguration;
import org.apache.hadoop.mapreduce.lib.db.DBInputFormat;
import org.apache.hadoop.mapreduce.lib.db.DBOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/1 21:21
 */
public class MysqlWCApp {

    static class WordCountMapper extends Mapper<LongWritable, MyDBWritable, Text, IntWritable> {

        @Override
        protected void map(LongWritable key, MyDBWritable value, Context context) throws IOException, InterruptedException {

            System.out.println("key:" + key.get());
            System.out.println(value.toString());
            String[] line = value.getText().split(" ");
            for (String word : line) {
                context.write(new Text(word), new IntWritable(1));
            }
        }
    }

    static class WordCountReducer extends Reducer<Text, IntWritable, MyDBWritable, NullWritable> {

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

            System.out.println(key);
            int count = 0;
            MyDBWritable myDBWritable = new MyDBWritable();
            for (IntWritable intWritable : values) {

                count += intWritable.get();
            }
            myDBWritable.setCount(count);
            myDBWritable.setWord(key.toString());
            context.write(myDBWritable, NullWritable.get());
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, SQLException {

        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "file:///");
        Job job = Job.getInstance(configuration);
        job.setJobName("mysql word count program");
        job.setJarByClass(MysqlWCApp.class);

        FileSystem fileSystem = FileSystem.newInstance(configuration);
        if (fileSystem.exists(new Path("D:\\mr\\out\\sql"))) {
            fileSystem.delete(new Path("D:\\mr\\out\\sql"), true);
        }

        //==========设置数据库连接============

        DBConfiguration.configureDB(job.getConfiguration(),
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://192.168.0.8:3306/house", "root", "123456");
        String query = "SELECT id,name,text FROM word";
//        DBInputFormat.setInput(job, MyDBWritable.class, "word", "id=1","","id","name","text");
        DBInputFormat.setInput(job, MyDBWritable.class, query, "select count(*) from word");

        //==========设置数据库连接 END ============

        job.setInputFormatClass(DBInputFormat.class);
        job.setMapperClass(WordCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setReducerClass(WordCountReducer.class);
        job.setNumReduceTasks(1);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        //输出到数据库中
        DBOutputFormat.setOutput(job, "wc", "word", "count");

        FileOutputFormat.setOutputPath(job, new Path("D:\\mr\\out\\sql"));
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
