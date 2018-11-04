package mooc;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/12 16:49
 */
public class LocalSumStormTopology {

    static class NumSpout extends BaseRichSpout {

        private TopologyContext context;
        private SpoutOutputCollector collector;
        private int number = 0;

        @Override
        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            this.context = context;
            this.collector = collector;
        }

        @Override
        public void nextTuple() {
            System.out.println("emit: " + number);
            collector.emit(new Values(number++));
            Utils.sleep(1000);
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("number"));
        }
    }

    static class SumBolt extends BaseBasicBolt {

        private int sum = 0;

        //这是个死循环，获取spout发送过来的数据
        @Override
        public void execute(Tuple input, BasicOutputCollector collector) {
            int value = (int) input.getValueByField("number");
            sum += value;
            System.out.println("value= " + value + "sum = " + sum + ",Thread=" + Thread.currentThread().getId());
            collector.emit(new Values(sum));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("value"));
        }
    }

    public static void main(String[] args) {

        LocalCluster cluster = new LocalCluster();
        TopologyBuilder builder = new TopologyBuilder();

        //设置spout - bolt 的执行顺序
        builder.setSpout("num-spout", new NumSpout());
        builder.setBolt("sum-bolt", new SumBolt()).shuffleGrouping("num-spout");

//        cluster.submitTopology("sum-topology", new HashMap(), builder.createTopology());

        Config config = new Config();
        config.setNumWorkers(4);
//        config.setNumAckers(0);
        try {
            StormSubmitter.submitTopology("sum-topology", config, builder.createTopology());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
