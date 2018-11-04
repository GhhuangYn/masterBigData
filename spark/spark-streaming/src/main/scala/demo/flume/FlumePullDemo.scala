package demo.flume

import org.apache.log4j.lf5.LogLevel
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description: flume和kafka整合的第二种方式：使用pull方式(常用)
  *               flume先把数据写入org.apache.spark.streaming.flume.sink.SparkSink中，
  *               然后spark streaming从该自定义的sink中抓取数据
  * @author: HuangYn
  * @date: 2018/10/31 16:38
  */
object FlumePullDemo {

  def main(args: Array[String]): Unit = {

    if (args.length < 2) {
      println("please enter <hostname> <port>")
      System.exit(1)
    }
    val Array(hostname, port) = args

    val conf = new SparkConf().setMaster("local[4]").setAppName("NetworkWordCount")
    val sc = new SparkContext(conf)
    sc.setLogLevel(LogLevel.WARN.getLabel)
    val ssc = new StreamingContext(sc, Seconds(5))

    val flumeStream = FlumeUtils.createPollingStream(ssc, hostname, port.toInt)
    flumeStream.map(e => {println(e.event);new String(e.event.getBody.array()).trim})
      .flatMap(_.split(" ")).map((_, 1)).reduceByKey(_ + _).print()

    ssc.start()
    ssc.awaitTermination()
  }

}
