package demo.kafka

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description: kafka与spark streaming整合   转换成RDD
  * @author: HuangYn 
  * @date: 2018/10/30 15:20
  */
object KafkaDemo2 {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[3]").setAppName("kafka integration")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")
    val ssc = new StreamingContext(sc, Seconds(10))

    //配置kafka属性
    val kafkaParams = new java.util.HashMap[String, Object]()
    kafkaParams.put("bootstrap.servers", "node00:9092,node03:9092")
    kafkaParams.put("key.deserializer", classOf[StringDeserializer])
    kafkaParams.put("value.deserializer", classOf[StringDeserializer])
    kafkaParams.put("group.id", "group1")
    kafkaParams.put("auto.offset.reset", "latest")
    kafkaParams.put("enable.auto.commit", false: java.lang.Boolean)

    /*
        If you have a use case that is better suited to batch processing,
        you can create an RDD for a defined range of offsets.
     */
   /* val offsetRanges = Array(
      // topic, partition, inclusive starting offset, exclusive ending offset
      OffsetRange("spark-kafka", 0, 0, 200), //获取分区0的信息，更精确地定义kafka的消费信息
      OffsetRange("spark-kafka", 1, 0, 200), //获取分区1的信息
      OffsetRange("spark-kafka", 2, 0, 200) //获取分区2的信息
    )
    val rdd = KafkaUtils.createRDD[String, String](
      sc, kafkaParams, offsetRanges, LocationStrategies.PreferConsistent)
    rdd.collect().foreach(println)*/

    ssc.start()
    ssc.awaitTermination()
  }

}

