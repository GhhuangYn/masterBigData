package kafka;

import org.apache.storm.kafka.bolt.KafkaBolt;
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
        String topic = (String) input.getValueByField("topic");
        int partition = (int) input.getValueByField("partition");
        long offset = (long) input.getValueByField("offset");
        String key = (String) input.getValueByField("key");
        String value = (String) input.getValueByField("value");
        System.out.println(topic + "," + partition + "," + offset + "," + key + "," + value);
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
