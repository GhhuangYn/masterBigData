package mooc.project.map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/**
 * @Description: 从kafka中获取数据进行格式化，然后输出到jdbcBolt中
 * @author: HuangYn
 * @date: 2018/10/13 22:39
 */
public class LogProcessBolt extends BaseRichBolt {

    private OutputCollector collector;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    //输入的tuple信息包括 [topic,partition,offset,key,value]
    @Override
    public void execute(Tuple input) {

        try {
            String value = (String) input.getValueByField("value");
            String info = value.split(" ")[2];
            String phone = info.split("\t")[0];
            String longtitude = info.split("\t")[1].split(",")[0];      //经度
            String latitude = info.split("\t")[1].split(",")[1];        //纬度
            String timestamp = info.split("\t")[2];
            System.out.println(phone + "," + longtitude + "," + latitude + "," + timestamp);
            this.collector.ack(input);
            this.collector.emit(new Values(Long.valueOf(timestamp), Double.valueOf(longtitude), Double.valueOf(latitude)));
        } catch (Exception e) {
            this.collector.fail(input);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("time", "longtitude", "latitude"));   //与数据库的字段对应
    }
}
