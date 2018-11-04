package hbase.demo1;

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

    private Map<String, Integer> wordMap = new HashMap<>();

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.context = context;
        this.collector = collector;
    }

    //切分一句话
    @Override
    public void execute(Tuple input) {
        String word = (String) input.getValueByField("word");
        Integer count = (Integer) input.getValueByField("count");
        if (!wordMap.containsKey(word)) {
            wordMap.put(word, 1);
        } else {
            wordMap.put(word, wordMap.get(word) + count);
        }
        collector.emit(new Values(word, wordMap.get(word)));
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
