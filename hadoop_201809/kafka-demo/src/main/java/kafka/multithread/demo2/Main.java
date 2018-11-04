package kafka.multithread.demo2;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/4 15:10
 */
public class Main {

    public static void main(String[] args) {
        final ConsumerThreadHandler<String, String> consumerThreadHandler =
                new ConsumerThreadHandler<>("node00:9092", "g1", "test1");
        final int cpuCount = Runtime.getRuntime().availableProcessors();
        Runnable r = () -> consumerThreadHandler.consume(cpuCount);
        new Thread(r).start();

        try {
            Thread.sleep(200000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Starting to close the consumer");
        consumerThreadHandler.close();
    }
}
