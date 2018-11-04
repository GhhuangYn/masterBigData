package demo.kafka

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext, TaskContext}

/**
  * @Description: kafka与spark streaming整合
  * @author: HuangYn 
  * @date: 2018/10/30 15:20
  */
object KafkaDemo {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[3]").setAppName("kafka integration")
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
  /*  val stream = KafkaUtils.createDirectStream(
      ssc,
      LocationStrategies.PreferConsistent, //持续均匀地分发partition给所有的executors
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams) //设置订阅信息
    )
    stream.map(record => (record.key(), record.value())).print()

    //获取位移offsets
    stream.foreachRDD(rdd => {
      //offsetRange包含了该rdd的(topic,partition,offset)信息
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges //把consumerRDD强转成kafkaRDD，并获取offset
      rdd.foreachPartition(it => {
        val o: OffsetRange = offsetRanges(TaskContext.getPartitionId())
        println(s"${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}")
      })
    })*/

    ssc.start()
    ssc.awaitTermination()
  }

}
