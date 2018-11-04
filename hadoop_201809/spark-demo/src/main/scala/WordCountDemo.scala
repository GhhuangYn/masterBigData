import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

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

object WordCountDemo {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setMaster("local[1]") //local[n]中的n表示线程并发数
    conf.setAppName("word count")
    val sc = new SparkContext(conf)

    sc.textFile("hdfs://node00:8020/root/wc.txt") //参数表示分区数
      .flatMap(_.split(" "))
      .map((_, 1))
      .reduceByKey(_ + _)
      .foreach(println)

    val rdd1 = sc.textFile("hdfs://node00:8020/root/wc.txt")
    rdd1.partitions.foreach(p=>println(p.index))
  }

}
