package redis;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.redis.bolt.RedisFilterBolt;
import org.apache.storm.redis.bolt.RedisStoreBolt;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.redis.common.mapper.RedisStoreMapper;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/13 17:43
 */
public class StoreRedisStorm {

    static class UserSpout extends BaseRichSpout {

        private SpoutOutputCollector collector;
        private HashMap<String, String> map = new HashMap<>();
        private int flag = 1;

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("id", "name"));
        }

        @Override
        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            this.collector = collector;
            map.put("1", "leo");
            map.put("2", "bobby");
            map.put("3", "gigi");
        }

        @Override
        public void nextTuple() {
            if (flag <= map.size()) {
                String name = map.get(String.valueOf(flag));
                System.out.println("nextTuple:" + String.valueOf(flag) + ":" + name);
                collector.emit(new Values(String.valueOf(flag), name));
                flag++;
            }
        }
    }

    public static void main(String[] args) {

        JedisPoolConfig poolConfig = new JedisPoolConfig.Builder()
                .setHost("node00").setPort(6379).build();
        RedisStoreMapper storeMapper = new UserStoreMapper();
        RedisStoreBolt storeBolt = new RedisStoreBolt(poolConfig, storeMapper);

        LocalCluster cluster = new LocalCluster();
        Config config = new Config();
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("spout", new UserSpout());
        builder.setBolt("userBolt", storeBolt).shuffleGrouping("spout");
        cluster.submitTopology("redis-topo", config, builder.createTopology());
    }
}
