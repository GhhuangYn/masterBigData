package demo

import org.apache.log4j.lf5.LogLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description: 窗口操作
  * @author: HuangYn 
  * @date: 2018/10/29 16:33
  */
object WindowDemo {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")
    val sc = new SparkContext(conf)
    sc.setLogLevel(LogLevel.WARN.getLabel)
    val ssc = new StreamingContext(sc, Seconds(1))
    val lines = ssc.socketTextStream("localhost", 6666)
    val words = lines.flatMap(_.split(" ")).map((_, 1))

    /*
        windowDuration - 窗口长度   slideDuration - 时间间隔
        window(windowDuration: Duration, slideDuration: Duration)
    */
    //    val wordCounts = words.reduceByKey(_ + _)
    //    wordCounts.window(Seconds(2), Seconds(3)) //每隔3s移动一个2s的窗口


    val wordCounts = words.reduceByKeyAndWindow(
      (a: Int, b: Int) => a + b, Seconds(2), Seconds(3)) //reduceByKey和window一起设置，效果相同

    //打印出来的是每隔3s对窗口开始位置的2s内的2个RDD进行统计单词数量
    wordCounts.print()
    wordCounts.saveAsTextFiles("G:\\data\\第6章\\vivi") //每滚动一次窗口保存一次数据

    ssc.start()
    ssc.awaitTermination()
  }
}
