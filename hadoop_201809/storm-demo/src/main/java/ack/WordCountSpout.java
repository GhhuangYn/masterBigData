package ack;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import wc.Util;

import java.util.*;

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
    private Map<String, String> messages = new HashMap<>();    //存放所有的消息
    private Map<String, Integer> failMessage = new HashMap<>();        //存放失败消息和次数

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
            //使用ack，发送时要指定messageId
            //使用时间戳作为消息ID
            String messageId = System.currentTimeMillis() + UUID.randomUUID().toString().substring(2, 5);
            messages.put(messageId, line);
            this.collector.emit(new Values(line), messageId);
        }
    }

    //下一级接收tuple成功，并且调用collector.ack()方法时会回调此方法
    @Override
    public void ack(Object msgId) {
        System.out.println("spout ack :" + msgId);
        messages.remove(msgId);
        if (failMessage.containsKey(msgId)) {
            failMessage.remove(msgId);
        }
    }

    //下一级接收tuple失败，并且调用collector.fial()方法时会回调此方法
    @Override
    public void fail(Object msgId) {
        //判断消息是否重试了3次
        Integer retries = failMessage.get(msgId) == null ? 0 : failMessage.get(msgId);
        if (retries < 3) {
            failMessage.put((String) msgId, ++retries);
            String originMsg = messages.get(msgId);
            collector.emit(new Values(originMsg), msgId);
            System.out.println("spout fail :" + msgId + " reties:" + retries);
        } else {
            //超过了重试次数就不重发，直接删除
            failMessage.remove(msgId);
            messages.remove(msgId);
            System.out.println("spout fail :" + msgId + " -----> end");
        }
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
