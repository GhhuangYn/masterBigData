package chapter4

import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/22 19:18
  */
object PartitionDemo {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName("test partitions")
    conf.setMaster("local")
    val sc = new SparkContext(conf)

    val user = sc.textFile("G:\\data\\chapter4\\第4章\\01-任务程序\\data\\user.txt")
    val rdd2 = user.map { x => val y = x.split(","); (y(0), y(1)) }
    val rdd3 = rdd2.partitionBy(new MyPartitioner(2))
    rdd3.mapPartitionsWithIndex((i,it)=>{println(s"$i");it.foreach(println);it}).count()
  }
}
