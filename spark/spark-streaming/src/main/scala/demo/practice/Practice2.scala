package demo.practice

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description: 过滤黑名单
  * @author: HuangYn 
  * @date: 2018/10/30 10:17
  */
object Practice2 {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[4]").setAppName("find error")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")
    val ssc = new StreamingContext(sc, Seconds(5))

    val mingdan = collection.mutable.Map[String, String]()
    val mingdanRDD = sc.textFile("G:\\data\\第6章\\02-习题答案\\data\\mingdan.txt") //(name ture)
      .map(line => {
      val pair = line.split(" ")
      (pair(0), pair(1))
    })
    mingdanRDD.foreach(println)

    val socketDS = ssc.socketTextStream("node00", 6666)
    socketDS.map(line => {
      (line.split(" ")(1), line.split(" ")(0))
    }).transform(rdd => {     //通过transform()对RDD进行操作
      rdd.leftOuterJoin(mingdanRDD).filter(_._2._2.get.contains("true"))
    }).print()

    ssc.start()
    ssc.awaitTermination()

  }
}
