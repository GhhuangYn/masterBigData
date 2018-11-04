package jdbc.store;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.hbase.bolt.mapper.SimpleHBaseMapper;
import org.apache.storm.jdbc.bolt.JdbcInsertBolt;
import org.apache.storm.jdbc.common.Column;
import org.apache.storm.jdbc.common.ConnectionProvider;
import org.apache.storm.jdbc.common.HikariCPConnectionProvider;
import org.apache.storm.jdbc.mapper.JdbcMapper;
import org.apache.storm.jdbc.mapper.SimpleJdbcMapper;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.ITuple;

import java.sql.JDBCType;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: JDBC于Storm整合 : 存储到数据库中
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

        //配置JdbcMapper:用于配置tuple和column之间的映射
        //构造器1 SimpleJdbcMapper(tableName, ConnectionProvider)
//        JdbcMapper jdbcMapper = new SimpleJdbcMapper("city", provider) {
//
              //如果上一级bolt或spout输出声明中字段的不一致，则需要重写这个方法
//            @Override
//            public List<Column> getColumns(ITuple tuple) {
//                System.out.println(tuple);
//                List<Column> columns = new ArrayList<>();
//                columns.add(new Column<>("city_name", tuple.getString(0), JDBCType.VARCHAR.getVendorTypeNumber()));
//                columns.add(new Column<>("city_code", tuple.getInteger(1), JDBCType.INTEGER.getVendorTypeNumber()));
//                return columns;
//            }
//        };

        //构造器2 使用SimpleJdbcMapper(List<Column> schemaColumns)
        //指定列集合，与OutputFieldsDeclarer声明的字段名对应
        List<Column> columnSchema = Lists.newArrayList(
                new Column("city_name", Types.VARCHAR),
                new Column("city_code", Types.INTEGER));
        SimpleJdbcMapper jdbcMapper = new SimpleJdbcMapper(columnSchema);

        //JdbcInsertBolt 插入mysql中的bolt
        JdbcInsertBolt jdbcBolt = new JdbcInsertBolt(provider, jdbcMapper)
                .withInsertQuery("insert into city(city_name,city_code) values(?,?)")
                .withQueryTimeoutSecs(100);

        Config config = new Config();
        config.setDebug(true);
        config.setNumWorkers(3);

        // wordSpout ==> splitBolt ==> HBaseBolt
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("city-spout", new CitySpout()).setNumTasks(1);
        builder.setBolt("jdbc-bolt", jdbcBolt).shuffleGrouping("city-spout");

        //本地模式调试
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("wc", config, builder.createTopology());
        Thread.sleep(6000);
        cluster.shutdown();

    }
}
