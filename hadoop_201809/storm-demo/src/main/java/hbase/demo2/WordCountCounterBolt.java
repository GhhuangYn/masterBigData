package hbase.demo2;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/5 18:43
 */
public class WordCountCounterBolt implements IRichBolt {

    private TopologyContext context;
    private OutputCollector collector;

    private Map<String, Long> wordMap = new HashMap<>();

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
//        Util.sendToClient(this, "prepare()","node01",8888);
        this.context = context;
        this.collector = collector;
    }

    //切分一句话
    @Override
    public void execute(Tuple input) {
        String word = (String) input.getValueByField("word");
        Long count = (Long) input.getValueByField("count");
        wordMap.put(word, count);
        System.out.println("counter: " + input);
    }

    @Override
    public void cleanup() {
        wordMap.forEach((k, v) -> System.out.println(k + ": " + v));
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
