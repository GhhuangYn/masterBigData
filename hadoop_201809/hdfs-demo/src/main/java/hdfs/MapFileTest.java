package hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.junit.Test;

import java.io.IOException;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/4 20:20
 */
public class MapFileTest {

    @Test
    public void save() throws IOException {

        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "file:///");
        FileSystem fileSystem = FileSystem.get(configuration);
        MapFile.Writer writer = new MapFile.Writer(configuration, fileSystem, "d:/b.map", IntWritable.class, Text.class);
        for (int i = 1; i < 20; i++)
            writer.append(new IntWritable(i), new Text("Tom-" + i));
        writer.close();
    }

    @Test
    public void read() throws IOException {

        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "file:///");
        FileSystem fileSystem = FileSystem.get(configuration);
        MapFile.Reader reader = new MapFile.Reader(fileSystem, "d:/b.map", configuration);
        IntWritable key = new IntWritable();
        Text value = new Text();
        while (reader.next(key, value)) {
            System.out.println(key.get() + ":" + value.toString());
        }
        reader.close();
    }
}
