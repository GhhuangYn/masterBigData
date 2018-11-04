package chapter4.practice

import java.time.{LocalDateTime, LocalTime}

import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description: 竞赛网站访问日志分析
  * @author: HuangYn 
  * @date: 2018/10/23 8:13
  */
object Practice {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("practice")
    val sc = new SparkContext(conf)
    val rdd1 = sc.textFile("G:\\data\\chapter4\\第4章\\03-实训数据\\jc_content_viewlog.txt")
    val rdd2 = rdd1.map(line => {
      val split_data = line.split(",")
      (split_data(0), split_data(1), split_data(2), split_data(3), split_data(5))
    }).persist()

    //    val websites = rdd2.map(_._2).distinct().count()
    //    println(s"被访问的网页数: $websites")
    //    val users = rdd2.map(_._4).distinct().count()
    //    println(s"访问的用户数: $users")
    val result = rdd2.map(t => (t._3.substring(0, 7), 1)).reduceByKey(new MonthPartition(3), _ + _)
    //    result.saveAsTextFile("G:\\data\\chapter4\\第4章\\03-实训数据\\result")

    //过滤出50次以上的用户记录
    val rdd3 = rdd2.map(t => (t._4, 1))
      .reduceByKey(_ + _)
      .filter(_._2 > 50).keys
      .persist(StorageLevel.MEMORY_ONLY)
    //rdd3.foreach(println)

    //统计访问50次以上的用户主要访问的前5类网页
    val rdd4 = rdd2.map(t => (t._4, (t._1, t._2, t._3, t._5)))
    //    rdd3.join(rdd4)
    //      .map(t => (t._2._1, (t._2._2._2, t._2._2._3)))
    //      .distinct()
    //      .sortByKey(ascending = true)
    //      .take(50)
    //      .foreach(println)

    val userIds = rdd3.collect()
    val top5 = rdd2.filter(x => userIds.contains(x._4))
      .map(t => ((t._2, t._3), 1))
      .reduceByKey(_ + _)
      .sortBy(x => x._2, ascending = false)
      .take(5)
    top5.foreach(println)

    //合并部分网页URL后面带有_1 _2字段的翻页网址
    rdd1.map(line => {
      if (line.contains("_"))
        line.replace("_", "")
      else
        line
    })

    //根据访问时间加入对应时段
    rdd2.map(x => (getPeriod(x._5), 1))
      .reduceByKey(_ + _)
      .foreach(println)


  }

  //根据访问时间加入对应时段
  def getPeriod(date: String): String = {

    val hour = date.split(" ")(1).split(":")(0).toInt
    val minute = date.split(" ")(1).split(":")(1).toInt
    val localTime = LocalTime.of(hour, minute)
    if (localTime.isAfter(LocalTime.of(6, 30)) && localTime.isBefore(LocalTime.of(11, 30))) {
      "上午"
    } else if (localTime.isAfter(LocalTime.of(11, 30)) && localTime.isBefore(LocalTime.of(14, 0))) {
      "中午"
    } else if (localTime.isAfter(LocalTime.of(14, 0)) && localTime.isBefore(LocalTime.of(17, 30))) {
      "下午"
    } else if (localTime.isAfter(LocalTime.of(17, 30)) && localTime.isBefore(LocalTime.of(19, 0))) {
      "傍晚"
    } else if (localTime.isAfter(LocalTime.of(19, 0)) && localTime.isBefore(LocalTime.of(23, 0))) {
      "晚上"
    } else
      "深夜"
  }


}
