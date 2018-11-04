package group.direct;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import wc.Util;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
        Map map = context.getThisTargets();
//        context.getTaskToComponent().forEach((k, v) -> System.out.println(k + ":" + v));

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
        if (counter < 8) {
            ++counter;
            String line = states.get(random.nextInt(4));

            //设置direct grouping,需要获取taskId -> 从context中获取

            //获取wc-split-bolt的任务Ids
            List<Integer> targetIds = new ArrayList<>();
            context.getTaskToComponent().forEach((k, v) -> {
                if (v.contains("wc-split-bolt")) {
                    targetIds.add(k);
                }
            });

            //随机获取一个Id
            int taskId = targetIds.get(2);
            //emitDirect()
            this.collector.emitDirect(taskId, new Values(line));
        }
    }

    @Override
    public void ack(Object msgId) {

    }

    @Override
    public void fail(Object msgId) {

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
