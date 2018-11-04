package demo.kafka

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description: kafka与spark streaming整合
  *              使用spark-streaming-kafka-0-10_2.11版本
  * @author: HuangYn 
  * @date: 2018/10/30 15:20
  */
object KafkaLogger {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[2]").setAppName("kafka integration")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")
    val ssc = new StreamingContext(sc, Seconds(10))

    //配置kafka属性
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "node00:9092,node03:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "group1",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val topics = Array("spark-kafka")


    val stream = KafkaUtils.createDirectStream(
      ssc,
      LocationStrategies.PreferConsistent, //持续均匀地分发partition给所有的executors
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams) //设置订阅信息
    )


    stream.map(record => {
      println(s"parititon: ${record.partition()} , value: ${record.value()}")
      record.value().split(":")(1).toInt
    }).reduce(_ + _).print()

    ssc.start()
    ssc.awaitTermination()
  }

}
