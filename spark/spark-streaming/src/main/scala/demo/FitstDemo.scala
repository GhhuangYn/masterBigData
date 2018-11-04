package demo

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/29 15:39
  */
object FitstDemo {

  def main(args: Array[String]): Unit = {

    /*
     本地模式中，线程数必须要大于输入源的个数，
     因为spark streaming会分配n个线程给receiver接收n个数据源的数据，
     还需要剩下一些线程用于处理数据
    */
    val conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount").set("logLevel", "WARN")
    val ssc = new StreamingContext(conf, Seconds(10)) //批处理的时间间隔为10s


    //创建一个DStream，使用TCP连接到指定地址接收数据
    val lines = ssc.socketTextStream("localhost", 6666)
    //监听文件目录(创建后修改的文件不保证可以监控到新的数据，因此每次有新数据重新创建文件比较安全)
    //    val lines = ssc.textFileStream("G:\\data\\第6章\\tmp\\")
    lines.print()
    //lines会在设定的时间中接收多行数据，需要分割每行的数据
    val words = lines.flatMap(_.split(" "))
    val wordCounts = words.map((_, 1)).reduceByKey(_ + _)
    wordCounts.print()

    ssc.start()
    ssc.awaitTermination() //等待计算结束

  }
}
