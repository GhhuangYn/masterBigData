import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.LocatedFileStatus;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Random;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/26 17:14
 */
public class GenerateLog {


    public static void main(String[] args) throws IOException {

        String host = "http://www.imooc.com/";
        Random random = new Random();
        String ip, url;
        int traffic;
        String[] type = {"video", "article"};
        String date;
        FileOutputStream fos = new FileOutputStream("G:\\access.log", true);

        for (int i = 0; i < 20000; i++) {
            traffic = random.nextInt(500);
            url = host + type[random.nextInt(2)] + "/" + random.nextInt(50);
            ip = random.nextInt(255) + "." + random.nextInt(255) + "." + random.nextInt(255) + "." + random.nextInt(255);
            date = "2017-08-22 " + LocalTime.of(random.nextInt(24), random.nextInt(60), random.nextInt(60));
            String data = date + "\t" + url + "\t" + traffic + "\t" + ip + "\r\n";
            IOUtils.write(data, fos, "UTF8");
        }
        fos.close();
    }
}
