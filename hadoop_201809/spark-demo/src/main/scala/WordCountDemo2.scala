import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

/**
  * @Description: 使用Scala和spark编写wordCount
  * @author: HuangYn 
  * @date: 2018/10/16 21:16
  */
/**
  * SparkContext: Spark功能的主要入口点，代表到spark的集群连接，可以创建RDD、累加器和广播变量
  * 每个JVM只能激活一个SparkContext
  * SparkConf:  Spark的配置对象
  */

object WordCountDemo2 {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setMaster("local")
    conf.setAppName("word count")
    val sc = new SparkContext(conf)



    //mapPartitions()是对分区进行处理，输入输出都是一个迭代器
    //    sc.textFile("G:\\wc.txt", 2).mapPartitions(it => {
    //      println("start")
    //      println("end")
    //      var l = ListBuffer[String]()
    //      for (elem <- it.next().split(" ")) {
    //        l += (elem + "!")
    //      }
    //      l.toIterator
    //    }).foreach(println)

    //rdd的变换(transform)的是一个lazy函数，只有使用真正使用时才会加载
    //而rdd的action是立刻执行的，比如reduce() count()

    val rdd1 = sc.textFile("G:\\wc.txt", 4)
    //mapPartitionsWithIndex()对每个分区的值进行迭代
    val rdd2 = rdd1.mapPartitionsWithIndex((i, it) => {
      println("start partition: " + i)
      println(Thread.currentThread().getName)
      it.map(_ + "!")
    })
    rdd2.foreach(println)

    val rdd3 = rdd1.flatMap(_.split(" ")).map((_, 1))
    val rdd4 = rdd3.reduceByKey(_ + _)
    val total = rdd4.map(_._2).reduce(_+_)    //统计所有单词个数
    println(s"total: $total")

    println(rdd3.count())   //count()

  }

}
