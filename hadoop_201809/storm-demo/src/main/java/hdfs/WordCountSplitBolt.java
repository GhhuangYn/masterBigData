package hdfs;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/5 18:43
 */
public class WordCountSplitBolt implements IRichBolt {

    private TopologyContext context;
    private OutputCollector collector;
    private Random random = new Random();

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.context = context;
        this.collector = collector;
    }

    //切分一句话
    @Override
    public void execute(Tuple input) {
        String line = input.getString(0);
        String[] words = line.split(" ");
        System.out.println("line: " + line);

        collector.ack(input);       //回调处理，如果成功，上一级的spout会接收到响应
        Arrays.stream(words).forEach(word -> {
            collector.emit(new Values(word, 1));
        });

    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word", "count"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
