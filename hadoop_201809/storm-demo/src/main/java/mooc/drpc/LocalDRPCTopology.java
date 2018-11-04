package mooc.drpc;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.LocalDRPC;
import org.apache.storm.drpc.LinearDRPCTopologyBuilder;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/13 15:37
 */
public class LocalDRPCTopology {

    static class MyBolt extends BaseRichBolt {

        private OutputCollector collector;

        @Override
        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
            this.collector = collector;
        }

        @Override
        public void execute(Tuple input) {
            Object requestId = input.getValue(0);
            System.out.println("requestId:"+requestId);
            String name = input.getString(1);
            String result = "add user:" + name;
            collector.emit(new Values(requestId, result));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("id", "result"));
        }
    }

    public static void main(String[] args) {
        LocalDRPC drpc = new LocalDRPC();
        LocalCluster cluster = new LocalCluster();

        LinearDRPCTopologyBuilder builder = new LinearDRPCTopologyBuilder("addUser");
        builder.addBolt(new MyBolt(),3);
        Config config = new Config();
        cluster.submitTopology("drpc-demo", config, builder.createLocalTopology(drpc));

        System.out.println("Results for 'addUser':" + drpc.execute("addUser", "leo"));

        cluster.shutdown();
        drpc.shutdown();
    }
}
