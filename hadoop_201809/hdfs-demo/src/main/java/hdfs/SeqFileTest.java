package hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;


/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/4 16:53
 */
public class SeqFileTest {

    @Test
    public void save() throws IOException {

        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "file:///");
        FileSystem fileSystem = FileSystem.get(configuration);
        SequenceFile.Writer writer = SequenceFile.createWriter(fileSystem, configuration, new Path("d:/a.seq"), IntWritable.class, Text.class);
        for (int i = 0; i < 100; i++) {
            writer.append(new IntWritable(i), new Text("Leo-" + i));
            if (i % 5 == 0) {
                writer.sync();
            }

        }
        writer.close();
    }

    @Test
    public void save2() throws IOException {

        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "file:///");
        FileSystem fileSystem = FileSystem.get(configuration);
        SequenceFile.Writer writer = SequenceFile.createWriter(
                fileSystem, configuration, new Path("d:/c.seq"), IntWritable.class, Text.class,
                SequenceFile.CompressionType.BLOCK);
        for (int i = 0; i < 100; i++) {
            writer.append(new IntWritable(i), new Text("Leo-" + i));
            if (i % 5 == 0) {
                writer.sync();
            }

        }
        writer.close();
    }

    @Test
    public void get() throws IOException {

        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "file:///");
        FileSystem fileSystem = FileSystem.get(configuration);
        SequenceFile.Reader reader = new SequenceFile.Reader(fileSystem, new Path("d:/mr/temp_part.lst"), configuration);
        IntWritable key = new IntWritable();
        IntWritable value = new IntWritable();
        while (reader.next(key, value)) {
            System.out.println(key.get() + ":" + value.get());
        }
        reader.close();
    }

    @Test
    public void get3() throws IOException {

        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "hdfs://node00:8020/");
        Path hdfsPath = new Path("hdfs://node00:8020/flumeData/18-09-28-/FlumeData.1538122943551");
        FileSystem fileSystem = FileSystem.get(configuration);
        SequenceFile.Reader reader = new SequenceFile.Reader(fileSystem, hdfsPath, configuration);
        LongWritable key = new LongWritable();
        BytesWritable value = new BytesWritable();
        while (reader.next(key, value)) {
            System.out.println(key.get());
            System.out.println(new String(value.getBytes(),0,value.getLength()));
        }
        reader.close();
    }

    @Test
    public void get2() throws IOException {

        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "file:///");
        FileSystem fileSystem = FileSystem.get(configuration);
        SequenceFile.Reader reader = new SequenceFile.Reader(fileSystem, new Path("d:/a.seq"), configuration);
        IntWritable key = new IntWritable();
        Text value = new Text();

        //文件指针
        System.out.println(reader.getPosition());

        reader.sync(300);

        while (reader.next(key)) {
            reader.getCurrentValue(value);
            System.out.println(reader.getPosition() + ":" + key.get() + ":" + value.toString());

        }
        reader.close();
    }

    @Test
    public void randomTemperature() throws IOException {

        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "file:///");
        FileSystem fileSystem = FileSystem.get(configuration);
        SequenceFile.Writer writer = SequenceFile.createWriter(fileSystem, configuration, new Path("d:/mr/temp.txt"), IntWritable.class, IntWritable.class);
        for (int i = 0; i < 1000; i++) {
            int year = 1930 + new Random().nextInt(100);
            int temperature = -20 + new Random().nextInt(60);
            writer.append(new IntWritable(year), new IntWritable(temperature));
        }
        writer.close();

    }

    @Test
    public void randomTemperature2() throws IOException {
        FileWriter fileWriter = new FileWriter("d:\\mr\\temp.txt");
        for (int i = 0; i < 1000; i++) {
            int year = 1930 + new Random().nextInt(100);
            int temperature = -20 + new Random().nextInt(60);
            fileWriter.write(year + " " + temperature + "\r\n");
            fileWriter.flush();
        }
        fileWriter.close();
    }

    @Test
    public void randomHello() throws IOException {
        FileWriter fileWriter = new FileWriter("d:\\mr\\1.txt");
        for (int i = 0; i < 100000; i++) {
            fileWriter.write("hello tom-" + i + "\r\n");
            fileWriter.flush();
        }
        fileWriter.close();
    }
}
