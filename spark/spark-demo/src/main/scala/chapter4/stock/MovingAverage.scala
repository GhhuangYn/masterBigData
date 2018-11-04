package chapter4.stock

import java.text.SimpleDateFormat

import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/22 20:15
  */
object MovingAverage {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setMaster("local[2]")
    conf.setAppName("calculate change")
    val sc = new SparkContext(conf)

    val sdf = new SimpleDateFormat("yyyy-MM-dd")
    val broadcastWindow = 10 //移动期数

    val stock = sc.textFile("G:\\data\\chapter4\\第4章\\01-任务程序\\data\\reform_data\\000002.csv", 1).filter(!_.contains("date"))
    val sortedRDD = stock.map(line => {
      val split_data = line.split(",")
      val timestamp = sdf.parse(split_data(0))
      (timestamp.getTime, (split_data(0), split_data(3).toDouble))
    }).sortByKey(ascending = true)
      .filter(_._2._2 != 0)
      .map(x => (x._2._1, x._2._2)) //经过日期排序的股票信息

    val queue = new mutable.Queue[Double]()
    var avg = 0.0
    val avgRdd = sortedRDD.map(x => {
      queue += x._2
      if (queue.size > broadcastWindow) {
        avg = (queue.sum - x._2 )/ broadcastWindow
        queue.dequeue()
        (x._1, avg, x._2)
      } else {
        (0, 0, 0)
      }
    }).persist(StorageLevel.MEMORY_AND_DISK)
    avgRdd.filter(_._1 != 0).map(x => (x._1, (x._2, x._3)))
      .partitionBy(new YearPartition(4))
      .saveAsTextFile("G:\\data\\chapter4\\第4章\\01-任务程序\\data\\reform_data\\broadcast")
  }
}
