package demo.kafka

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description: kafka与spark streaming整合
  * @author: HuangYn 
  * @date: 2018/10/30 15:20
  */
object KafkaDemo3 {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[3]").setAppName("kafka integration")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")
    val ssc = new StreamingContext(sc, Seconds(10))

    //配置kafka属性
    val kafkaParams = Map[String, String](
      "bootstrap.servers" -> "node00:9092,node03:9092",
      "group.id" -> "group1"
//      "auto.offset.reset" -> "latest",
//      "enable.auto.commit" -> "false"
    )
//    val topics = Array("spark-kafka")
//    val stream = KafkaUtils.createDirectStream(
//      ssc, kafkaParams,Set("spark-kafka"))
//    stream.map(record => (record._1, record._2)).print()

    ssc.start()
    ssc.awaitTermination()
  }

}
