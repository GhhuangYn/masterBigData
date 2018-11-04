package hdfs;

import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/5 10:50
 */
public class Util {

    //得到主机名
    public static String getHostName() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostName() + ":" + new String(inetAddress.getAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    //得到当前程序所在进程ID
    public static int getPid() {
        String info = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(info.substring(0, info.indexOf("@")));
    }

    //返回线程名称
    public static String getThreadName() {
        return Thread.currentThread().getName();
    }

    @Test
    public void TestRandom() {
        while (true)
            System.out.println(new Random().nextInt(10));
    }

}
