package bigadta.spark.streaming

import java.text.SimpleDateFormat
import java.util.Date

import bigadta.spark.mysql.MysqlManager
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._

object StreamPvClickAnalytics {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[4]").setAppName("UserClickCountStat")
    //设置时间间隔为5秒
    val ssc = new StreamingContext(conf, Seconds(5))
    //kafka参数设置
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "192.168.183.102:9092,192.168.183.103:9092,192.168.183.104:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "streampvclickcg4",
      "auto.offset.reset" -> "earliest",
      "enable.auto.commi" ->  (true: java.lang.Boolean)
    )
    //kafka中需要订阅的主题名称
    val topics = Array("userlog2")
    val kafkaStream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )
    //处理kafkaStream，解析数据
    val events = kafkaStream.map(record => {
      //数据格式:用户ID,客户端版本号,地域ID,用户行为,时间戳
      val datas = record.value.split(",")
      val sdf = new SimpleDateFormat("yyyyMMddHHmm")
      val time = sdf.format(new Date(datas(4).toLong))
      (time,datas(3).toInt)
    })
    events.cache()

    val pvDs = events.filter(_._2 == 1).map(x=>(x._1,1)).reduceByKey(_ + _)
    val clickDs = events.filter(_._2 == 2).map(x=>(x._1,1)).reduceByKey(_ + _)
    val reDs = pvDs.join(clickDs)
    reDs.foreachRDD(rdd => {
      rdd.foreachPartition(partitionOfRecords => {
        val conn = MysqlManager.getMysqlManager.getConnection
        val statement = conn.createStatement
        conn.setAutoCommit(false)
        try {
          partitionOfRecords.foreach(record => {
              val date_time = record._1
              val pv_cnt = record._2._1
              val click_cnt = record._2._2

              val sql = "insert into realtime_pv_click_report_daily (pv_cnt,click_cnt,date_time) " +
                "VALUE('" + pv_cnt + "','" + click_cnt + "','" + date_time + "')"
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
