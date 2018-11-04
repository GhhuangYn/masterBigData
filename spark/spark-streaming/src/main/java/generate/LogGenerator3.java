package generate;


import sun.misc.IOUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @Description: 实战课程的日志产生器
 * @author: HuangYn
 * @date: 2018/11/1 9:56
 */
public class LogGenerator3 {

    static Map<String, String> course = new HashMap<>();

    static {
        course.put("class/112.html", "Spark SQL 实战");
        course.put("class/145.html", "Hadoop基础");
        course.put("class/139.html", "Storm实战");
        course.put("class/123.html", "Spark Streaming实战");
        course.put("class/169.html", "大数据面试");
        course.put("course/list", "");
        course.put("learn/343", "");
    }


    public static void main(String[] args) throws IOException {

        String outpath = args[0];

        String[] urls = {"class/112.html", "class/145.html", "class/139.html", "class/123.html",
                "class/169.html", "course/list", "learn/343"};
        String[] http_referers = {
                "http://www.baidu.com/s?wd=",
                "https://www.sougou.com/web?query=",
                "http://cn.bing.com/search?q=",
                "https://search.yahoo.com/search?p=",
                "-" //未知
        };
        String[] keywords = {"Spark SQL 实战", "Hadoop基础", "Storm实战", "Spark Streaming实战", "大数据面试"};
        String[] status = {"200", "404", "500"};
        int[] ipNum = {133, 46, 22, 33, 87, 10, 45, 18, 224, 239, 52, 176, 38};
        int ipArrLen = ipNum.length;
        Random random = new Random();
        FileWriter writer = new FileWriter(outpath,true);

        for (int i = 0; i < 10; i++) {
            String url = urls[random.nextInt(urls.length)];
            String reference = "\"GET " + url + " HTTP/1.1\"";
            String http_referer = http_referers[random.nextInt(5)];
            if (!http_referer.equals("-")) {
                url = http_referer + course.get(url);
            }
            else url = "-";

            String ip = ipNum[random.nextInt(ipArrLen)] + "." + ipNum[random.nextInt(ipArrLen)]
                    + "." + ipNum[random.nextInt(ipArrLen)] + "." + ipNum[random.nextInt(ipArrLen)];
            SimpleDateFormat fdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = fdf.format(new Date());
            String log = ip + "\t" + date + "\t" + reference + "\t" + status[random.nextInt(3)] + "\t" + url;
//            System.out.println(log);

            writer.write(log+"\r\n");
            writer.flush();
        }
        writer.close();

    }
}
