package callLog;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: Call log counter bolt receives call and its duration as a tuple.
 * This bolt initializes a dictionary (Map) object in the prepare method. In execute method,
 * it checks the tuple and creates a new entry in the dictionary object for every new “call” value
 * in the tuple and sets a value 1 in the dictionary object.
 * @author: HuangYn
 * @date: 2018/10/5 16:02
 */
public class CallLogCounterBolt implements IRichBolt {

    Map<String, Integer> counterMap;
    private OutputCollector collector;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.counterMap = new HashMap<>();
        this.collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        String call = tuple.getString(0);
        Integer duration = tuple.getInteger(1);
        if (!counterMap.containsKey(call)) {
            counterMap.put(call, 1);
        } else {
            counterMap.put(call, counterMap.get(call) + 1);
        }

        //This method acknowledges that a specific tuple has been processed.
        collector.ack(tuple);
    }

    @Override
    public void cleanup() {
        for (Map.Entry<String, Integer> entry : counterMap.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("call"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
