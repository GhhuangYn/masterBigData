package compress;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.util.ReflectionUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/4 15:32
 */
public class CompressTest {

    public static void main(String[] args) throws IOException {
        System.out.println("==============ZIP===============");
        zip();
        System.out.println("==============UNZIP==============");
        unZip();
    }

    //压缩测试
    public static void zip() throws IOException {
        long start = System.currentTimeMillis();

        Class codecClass = DefaultCodec.class;
        Configuration configuration = new Configuration();
        CompressionCodec codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, configuration);
        CompressionOutputStream outputStream = codec.createOutputStream(new FileOutputStream("/root/zip/comp.def"));
        IOUtils.copy(new FileInputStream("/root/zip/comp.txt"), outputStream);
        outputStream.close();
    }

    public static void unZip() throws IOException {
        Class codecClass = DefaultCodec.class;
        Configuration configuration = new Configuration();
        CompressionCodec codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, configuration);
        CompressionInputStream inputStream = codec.createInputStream(new FileInputStream("/root/zip/comp.def"));
        IOUtils.copy(inputStream, new FileOutputStream("/root/zip/comp_unzip.txt"));
        inputStream.close();
    }

}
