package kafka.multithread.demo1;

/**
 * @Description:  每个consumer都放在独自一个线程中
 * @author: HuangYn
 * @date: 2018/10/4 12:52
 */
public class ConsumerMain {

    public static void main(String[] args) {
        String brokerList = "node00:9092";
        ConsumerGroup group = new ConsumerGroup(5, "g1", "test1", brokerList);
        group.execute();

        //结果显示，每个线程负责topic下的一个分区，如果消费线程数多余分区数，那么多处来的消费线程将不做任何操作。
    }
}
