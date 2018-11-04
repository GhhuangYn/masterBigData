package jdbc.lookup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.jdbc.bolt.JdbcInsertBolt;
import org.apache.storm.jdbc.bolt.JdbcLookupBolt;
import org.apache.storm.jdbc.common.Column;
import org.apache.storm.jdbc.common.ConnectionProvider;
import org.apache.storm.jdbc.common.HikariCPConnectionProvider;
import org.apache.storm.jdbc.mapper.JdbcMapper;
import org.apache.storm.jdbc.mapper.SimpleJdbcLookupMapper;
import org.apache.storm.jdbc.mapper.SimpleJdbcMapper;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.ITuple;
import org.apache.storm.tuple.Values;

import java.sql.JDBCType;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: JDBC于Storm整合 : 从mysql中获取数据  SimpleJdbcLookupMapper
 * @author: HuangYn
 * @date: 2018/10/5 18:52
 */
public class CityApp {

    public static void main(String[] args) throws InterruptedException, InvalidTopologyException, AuthorizationException, AlreadyAliveException {


        //配置数据源
        Map<String, Object> hikariConfigMap = Maps.newHashMap();
        hikariConfigMap.put("dataSourceClassName", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikariConfigMap.put("dataSource.url", "jdbc:mysql://node00/house");
        hikariConfigMap.put("dataSource.user", "root");
        hikariConfigMap.put("dataSource.password", "123456");
        ConnectionProvider provider = new HikariCPConnectionProvider(hikariConfigMap);

        //使用SimpleJdbcLookupMapper 绑定映射关系
        SimpleJdbcLookupMapper lookupMapper = new SimpleJdbcLookupMapper(
                new Fields("id","city_name","city_code"),     //输出格式,应该匹配sql语句中的查询字段
                Lists.newArrayList(                           //定义查询的列参数(相当于指定条件，对应sql),也可以重写toTuple()方法
//                        new Column("id", Types.INTEGER),
//                        new Column("city_name", Types.VARCHAR),
                        new Column("city_code", Types.INTEGER))) {
            @Override
            public List<Values> toTuple(ITuple input, List<Column> columns) {
//                System.out.println("tuple: "+input);
                return super.toTuple(input, columns);
            }
        };

        //JdbcLookupBolt 读取mysql数据
        String sql = "select id,city_name,city_code from city where city_code = ?";
        JdbcLookupBolt lookupBolt = new JdbcLookupBolt(provider, sql, lookupMapper)
                .withQueryTimeoutSecs(50);

        Config config = new Config();
        config.setDebug(true);
//        config.setNumWorkers(3);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("city-spout", new CitySpout()).setNumTasks(1);
        builder.setBolt("lookup-bolt", lookupBolt).shuffleGrouping("city-spout").setNumTasks(1);
        builder.setBolt("city-bolt",new CityBolt()).noneGrouping("lookup-bolt").setNumTasks(1);

        //本地模式调试
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("wc", config, builder.createTopology());
        Thread.sleep(6000);
        cluster.shutdown();

    }
}
