package generator;

import udp.HeartBeatThread;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/5 20:26
 */
public class Generator {

    public Generator() {
        new HeartBeatThread().start();
    }

    private String[] phoneNumbers = {"16675285890", "18813299487", "13727577495", "15971551004",
            "13422115728", "18565181597", "13080010932", "13265360128", "18777354343", "13559777534",
            "13406758563", "15602209451", "17798239564", "13118812701", "13147193238", "18819258466",
            "15768650644", "18826107967", "18271656032", "18256177568", "15521140991", "18879735663",
    };
    private Random random = new Random();

    public void getLog(FileWriter fileWriter) throws IOException {

        int len = phoneNumbers.length;
        String from = phoneNumbers[random.nextInt(len)],
                to = phoneNumbers[random.nextInt(len)];
        while (from.equals(to)) {
            to = phoneNumbers[random.nextInt(len)];
        }

        int year = 2018;
        int month = random.nextInt(9) + 1;
        int day = random.nextInt(28) + 1;
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        int sec = random.nextInt(60);
        LocalDateTime localDateTime = LocalDateTime.of(year, month, day, hour, minute, sec);
        long duration = random.nextInt(1000) + 1;

        //日志格式:  from,to,date,duration
        String log = from + "," + to + "," + localDateTime.toString() + "," + duration;
        System.out.println(log);
        fileWriter.write(log + "\r\n");
        fileWriter.flush();

    }


    public static void main(String[] args) throws IOException {

        if (args == null || args.length < 1) {
            System.out.println("please enter output path");
            System.exit(1);
        }
        Generator generator = new Generator();
        FileWriter fileWriter = new FileWriter(args[0], true);
        try {
            while (true) {
                generator.getLog(fileWriter);
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            fileWriter.close();
        } finally {
            fileWriter.close();
        }

    }


}
