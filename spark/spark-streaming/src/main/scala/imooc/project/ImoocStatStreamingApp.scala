package imooc.project

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.lf5.LogLevel
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

/**
  * @Description: 从kafka收集到日志信息
  * @author: HuangYn 
  * @date: 2018/11/1 19:35
  */
object ImoocStatStreamingApp {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[3]").setAppName("kafka-streaming")
    val sc = new SparkContext(conf)
    sc.setLogLevel(LogLevel.WARN.getLabel)
    val ssc = new StreamingContext(sc, Seconds(20))
    val kafkaParams = Map(
      "bootstrap.servers" -> "node00:9092,node03:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "group1",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val kafkaStream = KafkaUtils.createDirectStream(ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](Array("spark-kafka"), kafkaParams)
    )

    //输出格式 ClickLog(ip,date,classNo,status,from)
    val clickLogDStream = kafkaStream.map(record => record.value())
      .filter(_.split("\t")(2).contains("class/"))
      .map(line => {
        val logArr = line.split("\t")
        val date = logArr(1).replace("-", "").replace(" ", "").replace(":", "")
        val classNo = logArr(2).substring(logArr(2).indexOf("/") + 1, logArr(2).indexOf("."))
        ClickLog(logArr(0), date, classNo, logArr(3).toInt, logArr(4))
      })

    clickLogDStream.foreachRDD(rdd => {
      rdd.foreachPartition(it => {
        val listCCC = new ListBuffer[CourseClickCount] //这里的ListBuffer需要在每个partition中定义,
        val listCSCC = new ListBuffer[CourseSearchClickCount]
        it.foreach(clickLog => {
          println(clickLog.from)

          //需求1 统计当天至今的访问量
          val rowkeyCCC = clickLog.date.substring(0, 8) + "_" + clickLog.courseId
          listCCC += CourseClickCount(rowkeyCCC, 1)

          //需求2 每天到当前时间为止,从搜索引擎搜索进入课程的点击量
          if (!clickLog.from.equals("-")){
            val rowkeyCSCC = clickLog.date.substring(0, 8) + "_" + clickLog.from.split("/")(2)
            listCSCC += CourseSearchClickCount(rowkeyCSCC, 1)
          }

        })
        CourseClickDao.saveCourseClickCount(listCCC)
        CourseClickDao.saveCourseSearchClickCount(listCSCC)
      })
    })


    ssc.start()
    ssc.awaitTermination()
  }

}
