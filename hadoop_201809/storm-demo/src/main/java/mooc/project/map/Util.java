package mooc.project.map;

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/14 10:13
 */
public class Util {

    public static void generateLog() throws FileNotFoundException {

        Random random = new Random();
        double[][] locations = {
                {116.191031, 39.988585},
                {116.389275, 39.925818},
                {116.287444, 39.810742},
                {116.481707, 39.940089},
                {116.410588, 39.880172},
                {116.394816, 39.911812},
                {116.416002, 39.952917}};
        String[] phones = {"13660622922", "15920429756", "18819474357", "13422925258"};
        double[] location;
        String phone;
        int count = 0;
        String outPath = "/opt/logstash-5.6.1/maplog/access.log";
        FileOutputStream out = new FileOutputStream(outPath, true);
        try {
            while (true) {
                location = locations[random.nextInt(locations.length)];
                phone = phones[random.nextInt(phones.length)];
                String log = phone + "\t" + location[0] + "," + location[1]
                        + "\t" + System.currentTimeMillis() + System.lineSeparator();
                System.out.println(log);
                count++;
                out.write(log.getBytes());
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        generateLog();
    }
}