package callLog;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/**
 * @Description: Bolt is a component that takes tuples as input,
 *              processes the tuple, and produces new tuples as output.
 * @author: HuangYn
 * @date: 2018/10/5 15:55
 */
public class CallLogCreatorBolt implements IRichBolt{


    //Create instance for OutputCollector which collects and emits tuples to produce output
    private OutputCollector collector;

    /**
     * Provides the bolt with an environment to execute. The executors will run this method to initialize the spout.
     * @param map
     * @param topologyContext
     * @param outputCollector
     */
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
    }

    // Process a single tuple of input.
    //获取从spout传过来的元组信息，并进行重构
    @Override
    public void execute(Tuple tuple) {
        String from = tuple.getString(0);
        String to = tuple.getString(1);
        Integer duration = tuple.getInteger(2);
        collector.emit(new Values(from + " - " + to, duration));
    }

    //Called when a bolt is going to shutdown.
    @Override
    public void cleanup() {

    }

    //Declares the output schema of the tuple.
    //定义输出格式
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("call", "duration"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
