package generate;

import org.apache.log4j.Logger;

/**
 * @Description:  log4j ==> flume
 * @author: HuangYn
 * @date: 2018/10/31 20:14
 */
public class LoggerGenerator {

    private static Logger logger = Logger.getLogger(LoggerGenerator.class);

    public static void main(String[] args) throws InterruptedException {
        int i = 0;
        while (true) {
            Thread.sleep(1000);
            logger.info("value :" + i);
            i++;
        }
    }
}
