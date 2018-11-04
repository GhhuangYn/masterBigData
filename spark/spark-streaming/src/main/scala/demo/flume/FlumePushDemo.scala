package demo.flume

import org.apache.log4j.lf5.LogLevel
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @Description: flume和kafka整合的第一种方式：使用push方式
  *              flume使用avro sink的方式，直接将数据push到spark streaming中
  * @author: HuangYn 
  * @date: 2018/10/31 16:38
  */
object FlumePushDemo {

  def main(args: Array[String]): Unit = {

    if (args.length < 2) {
      println("please enter <hostname> <port>")
      System.exit(0)
    }
    val Array(hostname, port) = args

    val conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")
    val sc = new SparkContext(conf)
    sc.setLogLevel(LogLevel.WARN.getLabel)
    val ssc = new StreamingContext(sc, Seconds(5))

    val flumeStream = FlumeUtils.createStream(ssc, hostname, port.toInt)
    flumeStream.map(e => new String(e.event.getBody.array()).trim)
      .flatMap(_.split(" ")).map((_, 1)).reduceByKey(_ + _).print()

    ssc.start()
    ssc.awaitTermination()
  }

}
