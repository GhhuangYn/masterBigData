package mooc.project.map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.jdbc.bolt.JdbcInsertBolt;
import org.apache.storm.jdbc.common.Column;
import org.apache.storm.jdbc.common.ConnectionProvider;
import org.apache.storm.jdbc.common.HikariCPConnectionProvider;
import org.apache.storm.jdbc.mapper.JdbcMapper;
import org.apache.storm.jdbc.mapper.SimpleJdbcMapper;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.topology.TopologyBuilder;

import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/10/13 22:27
 */
public class StormKafkaTopo {

    public static void main(String[] args) {

        //============kafkaSpout=============
        KafkaSpoutConfig.Builder<String, String> kafkaBuilder =
                KafkaSpoutConfig.builder("node00:9092,node01:9092", "kafka_storm_topic");

        //设置每次读取的开始位置
        kafkaBuilder.setFirstPollOffsetStrategy(KafkaSpoutConfig.FirstPollOffsetStrategy.LATEST);
        //取出数据之后立刻提交offset，避免重复消费
        kafkaBuilder.setProcessingGuarantee(KafkaSpoutConfig.ProcessingGuarantee.NO_GUARANTEE);

        KafkaSpoutConfig<String, String> kafkaSpoutConfig = new KafkaSpoutConfig<>(kafkaBuilder);
        KafkaSpout<String, String> kafkaSpout = new KafkaSpout<>(kafkaSpoutConfig);


        //=============JDBC Bolt=================
        Map<String, Object> hikariConfigMap = Maps.newHashMap();
        hikariConfigMap.put("dataSourceClassName", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikariConfigMap.put("dataSource.url", "jdbc:mysql://node00/house");
        hikariConfigMap.put("dataSource.user", "root");
        hikariConfigMap.put("dataSource.password", "123456");
        ConnectionProvider provider = new HikariCPConnectionProvider(hikariConfigMap);
        List<Column> columnSchema = Lists.newArrayList(
                new Column("time", Types.BIGINT),
                new Column("longtitude", Types.DOUBLE),
                new Column("latitude", Types.DOUBLE));
        SimpleJdbcMapper jdbcMapper = new SimpleJdbcMapper(columnSchema);
        JdbcInsertBolt jdbcInsertBolt = new JdbcInsertBolt(provider, jdbcMapper)
                .withInsertQuery("insert into stat values(?,?,?)");

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("kafka-spout", kafkaSpout, 3).setNumTasks(2);
        builder.setBolt("logProcess-bolt", new LogProcessBolt()).shuffleGrouping("kafka-spout");
        builder.setBolt("jdbcInsertBolt", jdbcInsertBolt).shuffleGrouping("logProcess-bolt");

        LocalCluster cluster = new LocalCluster();
        Config config = new Config();
        cluster.submitTopology("StormKafkaTopo", config, builder.createTopology());

    }
}
