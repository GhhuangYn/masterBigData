package hdfs;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/5 18:30
 */
public class WordCountSpout implements IRichSpout {

    private SpoutOutputCollector collector;
    private TopologyContext context;
    private Random random = new Random();

    private List<String> states = new ArrayList<>();
    private int counter = 0;

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.context = context;
        this.collector = collector;
        states.add("hello hadoop hello leo hello");
        states.add("hello storm");
        states.add("hello hadoop hbase");
        states.add("hello spark");
    }

    @Override
    public void close() {

    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public void nextTuple() {

        //随机取出一句话
        if (counter < 10) {
            ++counter;
            String line = states.get(random.nextInt(4));

            //storm确保消息如何被完成处理
            //使用ack，发送时要指定messageId,这里使用counter作为messageId
            this.collector.emit(new Values(line), counter);
        }
    }

    //下一级接收tuple成功，并且调用collector.ack()方法时会回调此方法
    @Override
    public void ack(Object msgId) {
        System.out.println("spout ack :" + msgId);
    }

    //下一级接收tuple失败，并且调用collector.fial()方法时会回调此方法
    @Override
    public void fail(Object msgId) {
        System.out.println("spout fail :" + msgId);
    }

    //声明输出的格式
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("line"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
