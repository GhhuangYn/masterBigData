package demo.practice

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description: 统计error的记录数
  * @author: HuangYn 
  * @date: 2018/10/30 10:17
  */
object Practice1 {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[4]").setAppName("find error")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")
    val ssc = new StreamingContext(sc, Seconds(5))
    val socketDS = ssc.socketTextStream("localhost", 6666)
    socketDS.flatMap(_.split(" ")).filter(_.equals("error")).count().print()
    ssc.start()
    ssc.awaitTermination()

  }
}
