package demo

import java.sql.{Date, DriverManager}

import org.apache.log4j.lf5.LogLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description: foreachPartition()与mysql连接的合理用法
  * @author: HuangYn 
  * @date: 2018/10/29 17:18
  */
object ActionDemo {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")
    val sc = new SparkContext(conf)
    sc.setLogLevel(LogLevel.WARN.getLabel)
    val ssc = new StreamingContext(sc, Seconds(5))
    val itemsStream = ssc.socketTextStream("localhost", 6666)
    val itemPairs = itemsStream.map(line => (line.split(",")(0), 1))
    val itemCount = itemPairs.reduceByKeyAndWindow((a: Int, b: Int) => a + b, Seconds(30), Seconds(10))

    //获取到每个窗口的前三个热词
    val hottestWord = itemCount.transform(itemRDD => {
      val top3 = itemRDD.map(pair => (pair._2, pair._1))
        .sortByKey(ascending = false)
        .map(pair => (pair._2, pair._1))
        .take(3)
      ssc.sparkContext.makeRDD(top3)
    })
    //存入数据库,遍历每个rdd,然后遍历每个rdd的分区，在分区中使用mysql的连接对象输入rdd分区中的数据到外部系统
    hottestWord.foreachRDD(rdd => {

      //在每个分区中创建mysql连接
      rdd.foreachPartition(partition => {
        Class.forName("com.mysql.jdbc.Driver")
        val connection = DriverManager.getConnection(
          "jdbc:mysql://node00:3306/imooc?characterEncoding=UTF8&useSSL=false",
          "root",
          "123456")
        val pst = connection.prepareStatement("insert into searchKeyWord values(?,?,?)")
        connection.setAutoCommit(false)

        //遍历每个分区中的记录
        partition.foreach(record => {
          println("record: "+record)
          pst.setDate(1, new Date(System.currentTimeMillis()))
          pst.setString(2, record._1)
          pst.setInt(3, record._2)
          pst.addBatch()
        })
        pst.executeBatch()
        connection.commit()
      })
    })
    hottestWord.print()
    ssc.start()
    ssc.awaitTermination()



    /*
       foreachPartition()用法
       通过foreachPartition()将处理结果写道MySQL中
     */


  }
}
