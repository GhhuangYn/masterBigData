package generate;

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/29 19:45
 */
public class LogGenerator2 {

    public static void main(String[] args) throws IOException {

        FileWriter fos = new FileWriter("G:\\test.log");
        DecimalFormat decimalFormat = new DecimalFormat("0000");
        DecimalFormat decimalFormat2 = new DecimalFormat("#.0");
        Random random = new Random();
        int[] likes = {-1, 1, 0};
        for (int i = 0; i < 10000; i++) {
            String pageId = decimalFormat.format(random.nextInt(10000)) + ".html";
            int userRank = random.nextInt(8) + 1;
            int visitTimes = random.nextInt(9) + 1;
            float waitTime = Float.parseFloat(decimalFormat2.format((random.nextInt(9) + 1) / 10.0));
            int like = likes[random.nextInt(3)];
            IOUtils.write(pageId + "," + userRank + "," + visitTimes + "," + waitTime + "," + like + "\r\n", fos);
        }

    }
}
