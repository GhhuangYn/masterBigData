package demo.task

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/29 20:17
  */
object Task {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[3]").setAppName("task")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")
    val ssc = new StreamingContext(sc, Seconds(5))
    val lines = ssc.textFileStream("file:///G:\\data\\streaming")
    //    val lines = ssc.textFileStream("hdfs://node00:8020/spark/streaming")

    //计算网页热度代码
    val hotDS = lines.map(line => {
      val records = line.split(",")
      val hot = 0.1 * records(1).toInt + 1.9 * records(2).toInt +
        0.4 * records(3).toFloat + records(4).toInt
      (records(0), hot)
    })
    //    val hot = hotDS.window(Seconds(60), Seconds(10))
    val hot = hotDS.reduceByKeyAndWindow((v1: Double, v2: Double) => v1 + v2, Seconds(60), Seconds(10))
    val hottestHtml = hot.transform(itemRDD => {
      val hotArray = itemRDD
        //        .reduceByKey(_ + _)
        .map(item => (item._2, item._1))
        .sortByKey(ascending = false)
        .map(item => (item._2, item._1))
        .take(10)
      sc.makeRDD(hotArray).repartition(1)
    })

    //把网页热度输出到mysql
    hottestHtml.foreachRDD(rdd => {
      println("RDD:" + rdd.collect().mkString(","))
      rdd.foreachPartition(partition => {
        val conn = MySqlUtil.getConn()
        MySqlUtil.truncate(conn, "top_web_page") //每次插入前清除原来的数据
        conn.setAutoCommit(false)
        val pst = conn.prepareStatement("insert into top_web_page values(?,?,?)")
        var rank = 1
        partition.foreach(item => {
          pst.setInt(1, rank)
          pst.setString(2, item._1)
          pst.setDouble(3, item._2)
          rank += 1
          pst.addBatch()
        })
        pst.executeBatch()
        conn.commit()
        MySqlUtil.release(conn, pst)
      })

    })

    ssc.start()
    ssc.awaitTermination()

  }
}
