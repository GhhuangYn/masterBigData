package kafka.multithread.demo1;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 消费线程管理类，创建多个线程类执行消费任务
 * @author: HuangYn
 * @date: 2018/10/4 12:45
 */
public class ConsumerGroup {

    private List<ConsumerRunnable> consumerList;

    public ConsumerGroup(int consumerNum, String groupId, String topic, String brokerList) {
        consumerList = new ArrayList<>();
        for (int i = 0; i < consumerNum; i++) {
            consumerList.add(new ConsumerRunnable(brokerList, groupId, topic));
        }
    }

    public void execute() {
        consumerList.forEach(r -> new Thread(r).start());
    }
}
