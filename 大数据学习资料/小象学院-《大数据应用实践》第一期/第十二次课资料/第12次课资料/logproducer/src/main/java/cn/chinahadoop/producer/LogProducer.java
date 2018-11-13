package cn.chinahadoop.producer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 模拟客户端生产日志
 * Created by xiaoguanyu on 2018/1/25.
 */
public class LogProducer {
    //获取客户端版本号
    public static String getClientVersion(int userNum) {
        List<String> clientVersions = new ArrayList<String>();
        clientVersions.add("apple_phone_v1.2_20180101");
        clientVersions.add("android_v1.2_20180110");
        clientVersions.add("windows_v1_20171201");
        clientVersions.add("apple_pad_v1.2_20180101");
        clientVersions.add("apple_mac_v1.2_20180101");
        clientVersions.add("android_pad_v1.2_20180110");
        //通过用户编号与客户端版本个数取余的方式，保证同一个用户获取到的客户端版本是相同的
        int index = userNum % clientVersions.size();
        String clientVersion = clientVersions.get(index);
        return clientVersion;
    }
    //获取用户地域编号
    public static String getUserArea(int userNum) {
        List<String> areas = new ArrayList<String>();
        areas.add("010");//北京
        areas.add("021");//上海
        areas.add("020");//广州
        areas.add("0755");//深圳
        //通过用户编号与地域个数取余的方式，保证同一个用户获取到的地域编码是相同的
        int index = userNum % areas.size();
        String userArea = areas.get(index);
        return userArea;
    }
    public static void wirteFile(String fileName) throws IOException {
        BufferedWriter writer = null;
        try {
            //用于标识写入的数据是否换行
            int m = 0;
            File file = new File(fileName);
            //如果向已经存在的文件写数据应该先换行
            if(file.exists()){
                m = 1;
            }
            writer  = new BufferedWriter(new FileWriter(file,true));
            Random random = new Random();
            while(true){
                int userNum = random.nextInt(100);
                String userId = "user_" + userNum;
                String clientVersion = getClientVersion(userNum);
                String userArea = getUserArea(userNum);
                int userBehavior = 1;//1表示曝光,2表示点击
                //模拟点击事件
                if(m%3 == 0){
                    userBehavior = 2;
                }
                long eventTime = System.currentTimeMillis();
                //用户ID,客户端版本号,地域ID,用户行为,时间戳
                String line = userId + "," +  clientVersion + "," + userArea + "," + userBehavior + "," + eventTime;
                System.out.println("line -> " + line);
                //如果不是新文件的第一行，只要添加一行数据在添加新数据之前换行
                if(m != 0){
                    writer.newLine();
                }
                writer.write(line);
                writer.flush();
                Thread.sleep(1000);
                m++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static void main(String[] args){
        try {
            String filePath = args[0];
            String filePrefix = args[1];
            //日期格式化yyyyMMddHHmmss
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String dateStr = sdf.format(new Date());
            String fileName = filePath + "/" + filePrefix + dateStr + ".log";
            wirteFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
