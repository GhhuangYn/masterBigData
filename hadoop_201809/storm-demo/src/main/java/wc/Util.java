package wc;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/5 19:24
 */
public class Util {


    //获取主机名
    static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取pid
    static String getPid() {
        String info = ManagementFactory.getRuntimeMXBean().getName();
        return info.split("@")[0];
    }

    //获取线程名称
    static String getThreadId() {
        return Thread.currentThread().getName();
    }

    //对象信息
    static String getClassInfo(Object obj) {
        String name = obj.getClass().getSimpleName();
        int hash = obj.hashCode();
        return name + "@" + hash;
    }

    //
    static String getInfo(Object obj, String msg) {
        return getHostname() + "," + getPid() + "," + getThreadId()
                + "," + getClassInfo(obj) + "," + msg;
    }

    public static void sendToClient(Object obj, String msg, String host, int port) {
        try {
            String info = getInfo(obj, msg);
            Socket socket = new Socket(host, port);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write((info + "\n\r").getBytes());
            outputStream.flush();
            outputStream.close();
//            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Socket socket;

    static {
        try {
            socket = new Socket("localhost",6667);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendToLocalClient(Object obj, String msg, int port) {
        try {
            String info = getInfo(obj, msg);
//            Socket socket = new Socket("localhost", port);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write((info + "\n\r").getBytes(), 0, (info + "\n\r").getBytes().length);
            outputStream.flush();
//            outputStream.close();
//            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
