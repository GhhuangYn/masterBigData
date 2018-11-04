package hdfs;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.util.ReflectionUtils;
import org.junit.Test;
import sun.security.krb5.Config;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/8/30 16:10
 */
public class HdfsTest {


    @Test
    public void test1() throws IOException {

        //注册hdfs协议
        URL.setURLStreamHandlerFactory(new FsUrlStreamHandlerFactory());

        URL url = new URL("hdfs://node01:9000/NOTICE.txt");
        URLConnection connection = url.openConnection();
        InputStream inputStream = connection.getInputStream();
        byte[] bytes = new byte[1024];
        int len;
        while ((len = inputStream.read(bytes)) > 0) {
            System.out.println(new String(bytes, 0, len));
        }
        inputStream.close();
    }

    //通过hadoopAPI访问
    @Test
    public void test2() throws IOException {

        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "hdfs://node00:8020");
        FileSystem fileSystem = FileSystem.get(configuration);
        FSDataInputStream inputStream = fileSystem.open(new Path("/NOTICE.txt"));
        byte[] bytes = new byte[1024];
        int len;
        while ((len = inputStream.read(bytes)) > 0) {
            System.out.println(new String(bytes, 0, len));
        }
    }

    @Test
    public void test3() throws IOException {

        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "hdfs://node00:8020");
        FileSystem fileSystem = FileSystem.get(configuration);
        FSDataInputStream inputStream = fileSystem.open(new Path("/NOTICE.txt"));
        List<String> list = IOUtils.readLines(inputStream);
        list.forEach(System.out::println);
    }

    @Test
    public void test4() throws IOException {

        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", "hdfs://node00:8020");
        FileSystem fileSystem = FileSystem.get(configuration);
//        RemoteIterator<LocatedFileStatus> iterator = fileSystem.listFiles(new Path("/"), false);
        FileStatus[] array = fileSystem.listStatus(new Path("/"));
        Arrays.stream(array).forEach((fileStatus -> {

            System.out.println(fileStatus.getPath() + " " + fileStatus.getPermission() +
                    " " + fileStatus.getReplication() + " " + fileStatus.isDirectory());
        }));

    }

    @Test
    public void test5() throws IOException {

        Configuration configuration = new Configuration();
        FileSystem fileSystem = FileSystem.get(configuration);
        fileSystem.copyToLocalFile(new Path("/NOTICE.txt"), new Path("D:\\b.txt"));
    }

    //========添加了配置文件=========
    @Test
    public void test6() throws IOException {
        Configuration configuration = new Configuration();
        FileSystem fileSystem = FileSystem.get(configuration);
        FSDataInputStream inputStream = fileSystem.open(new Path("/NOTICE.txt"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, baos);
        inputStream.close();
        baos.close();
        System.out.println(new String(baos.toByteArray()));
    }

    @Test
    public void test7() throws IOException {
        Configuration configuration = new Configuration();
        FileSystem fileSystem = FileSystem.get(configuration);
        FSDataOutputStream outputStream = fileSystem.create(new Path("/a.txt"));
        byte[] bytes = "hello world".getBytes();
        outputStream.write(bytes, 0, bytes.length);
        outputStream.close();
    }

    @Test
    public void test8() throws IOException {
        Configuration configuration = new Configuration();
        FileSystem fileSystem = FileSystem.get(configuration);
        fileSystem.delete(new Path("/a.txt"), true);
    }

    //每个文件可以指定副本数和块大小
    @Test
    public void test9() throws IOException {
        Configuration configuration = new Configuration();
        FileSystem fileSystem = FileSystem.get(configuration);
        FSDataOutputStream outputStream = fileSystem.create(new Path("/a.txt"), true, 1024, (short) 1, 1024 * 1024);
        byte[] bytes = "hello world".getBytes();
        outputStream.write(bytes, 0, bytes.length);
        outputStream.close();

    }

    //压缩测试
    @Test
    public void zip() throws IOException {
        long start = System.currentTimeMillis();

        Class codecClass = DefaultCodec.class;
        Configuration configuration = new Configuration();
        CompressionCodec codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, configuration);
        CompressionOutputStream outputStream = codec.createOutputStream(new FileOutputStream("d:\\wc.default"));
        IOUtils.copy(new FileInputStream("d:\\wc.txt"), outputStream);
        outputStream.close();
    }

    @Test
    public void unZip() throws IOException {
        Class codecClass = DefaultCodec.class;
        Configuration configuration = new Configuration();
        CompressionCodec codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, configuration);
        CompressionInputStream inputStream = codec.createInputStream(new FileInputStream("D:\\wc.default"));
        IOUtils.copy(inputStream, new FileOutputStream("D:\\wb.txt"));
        inputStream.close();
    }
}
