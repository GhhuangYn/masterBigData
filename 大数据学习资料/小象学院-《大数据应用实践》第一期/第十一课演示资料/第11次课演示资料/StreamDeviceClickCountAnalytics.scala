package bigadta.spark.streaming

import java.text.SimpleDateFormat
import java.util.Date

import bigadta.spark.mysql.MysqlManager
import org.apache.spark.SparkConf
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe


object StreamDeviceClickCountAnalytics {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[4]").setAppName("UserClickCountStat")
    //设置时间间隔为5秒
    val ssc = new StreamingContext(conf, Seconds(5))
    //kafka参数设置
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "192.168.183.102:9092,192.168.183.103:9092,192.168.183.104:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "deviceClickstream",
      "auto.offset.reset" -> "earliest",
      "enable.auto.commi" ->  (true: java.lang.Boolean)
    )
    //kafka中需要订阅的主题名称
    val topics = Array("DeviceEvents")
    val kafkaStream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )
    //处理kafkaStream，解析数据
    val events = kafkaStream.map(record => {
      //数据格式:deviceId|deviceType|time|click
      val datas = record.value.split("\\|")
      val deviceType = datas(1)
      val sdf = new SimpleDateFormat("yyyyMMddHHmm")
      val time = sdf.format(new Date(datas(2).toLong))
      println("**********time->" + time)
      val typeTime = datas(0) + "|" + deviceType + "|" + time
      (typeTime,datas(3).toInt)
    })

    //计算点击量
    val typeTimeClicks = events.reduceByKey(_ + _)

    /*
    性能很差的写法
    通常在连接数据库的时候会使用连接池
    问题：
    在每个rdd的每条记录当中都进行了connection的建立和关闭，这会导致不必要的高负荷并且降低整个系统的吞吐量。
    所以一个更好的方式是使用_rdd.foreachPartition_即对于每一个rdd的partition建立唯一的连接
    */
   /* typeTimeClicks.foreachRDD(rdd => {
      rdd.foreach(record => {
          val conn = MysqlManager.getMysqlManager.getConnection
          val statement = conn.createStatement
          val deviceTypeTime = record._1.split("\\|")
          val deviceId = deviceTypeTime(0)
          val deviceType = deviceTypeTime(1)
          val time = deviceTypeTime(2)
          val clickCount = record._2
          val sql = "insert into device_click (device_id,device_type,time,click_count) " +
            "VALUE('" + deviceId + "','" + deviceType + "','" + time + "','" + clickCount + "')"
          println("sql -> " + sql)
          statement.execute(sql)
          statement.close()
          conn.close()
      })
    })*/
    //优化升级
    //降低了频繁建立连接的负载
    typeTimeClicks.foreachRDD(rdd => {
      rdd.foreachPartition(partitionOfRecords => {
        val conn = MysqlManager.getMysqlManager.getConnection
        val statement = conn.createStatement

        //为了进一步提高mysql的效率，在提交Mysql的操作的时候，并不是每条记录提交一次，而是采用了批量提交的形式
        //取消自动提交，所以需要将conn.setAutoCommit(false)
        conn.setAutoCommit(false)
        try {
          partitionOfRecords.foreach(record => {
              val deviceTypeTime = record._1.split("\\|")
              val deviceId = deviceTypeTime(0)
              val deviceType = deviceTypeTime(1)
              val time = deviceTypeTime(2)
              val clickCount = record._2

              val sql = "insert into device_click (device_id,device_type,time,click_count) " +
                "VALUE('" + deviceId + "','" + deviceType + "','" + time + "','" + clickCount + "')"
              println("sql -> " + sql)
              statement.addBatch(sql)
          })
          statement.executeBatch
          conn.commit
        } catch {
          case e: Exception => println("error:" + e)
        } finally {
          statement.close()
          conn.close()
        }
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }
}
