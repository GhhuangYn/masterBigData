package mooc.log

import mooc.log.dao.StatDao
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable.ListBuffer

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/26 21:41
  */
object TopNStatJobOnYarn {


  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .config("spark.sql.sources.partitionColumnTypeInference.enabled", value = false) //关闭自动推倒数据类型
      .getOrCreate()

    if (args.length<2){
      System.exit(0)
    }

    val data = spark.read.parquet(args(0))
    data.printSchema()
    //data.show()


    //每次测试先删除数据库中的所有数据
    val day = args(1)
    StatDao.deleteData(day)

    //最受欢迎的视频topN课程(某一天)
    videoAccessTopNStat(spark, day, data)

    //按照地市进行统计topN课程(按城市统计)
    videoAccessTopNStatOfCity(spark, day, data)

    //按照流量进行统计
    videoTrafficTopN(spark, day, data)

    spark.stop()

  }

  //先统计，再保存到数据库

  /**
    * 最受欢迎的视频topN课程(某一天)
    */

  import org.apache.spark.sql.functions._

  def videoAccessTopNStat(spark: SparkSession, day: String, accessDF: DataFrame): Unit = {
    //    val videoAccessTopN = accessDF.filter($"day" === "20170819" && $"cmsType" === "video")
    //      .groupBy("cmsId")
    //      .agg(count("cmsId").as("times"))
    //      .orderBy($"times".desc)
    //    videoAccessTopN.show(false)

    //使用sql
    accessDF.createOrReplaceTempView("videoRecord")
    val result = spark.sql("select day,cmsId ,count(*) times from videoRecord " +
      s"where day='$day' and cmsType='video' group by day,cmsId order by times desc")
    result.show(false)

    //保存到mysql中
    StatDao.saveDayVideoAccessTopNStatToMysql(result.collect())

  }


  /**
    * 最受欢迎的视频topN课程,按照某一天，某个城市统计
    */
  def videoAccessTopNStatOfCity(spark: SparkSession, day: String, data: DataFrame): Unit = {
    import spark.implicits._
    val cityTopNDF = data.filter(s"day='$day' and cmsType='video'")
      .groupBy("day", "city", "cmsId")
      .agg(count("cmsId").as("times"))

    //每个城市中要进行排序统计,使用window函数row_number()
    val cityTopNDFWithOrder = cityTopNDF.select(cityTopNDF("day"),
      cityTopNDF("city"),
      cityTopNDF("cmsId"),
      cityTopNDF("times"),
      row_number().over(Window.partitionBy(cityTopNDF("city")).orderBy($"times".desc)).as("times_rank")
    ).filter("times_rank <= 3")

    cityTopNDFWithOrder.show(40)

    //保存到mysql中
    try {
      val list = new ListBuffer[DayVideoCityAccessTopNStat]()
      cityTopNDFWithOrder.collect().foreach(row => {
        list.append(DayVideoCityAccessTopNStat(
          row.getAs[String]("day"),
          row.getAs[Long]("cmsId"),
          row.getAs[String]("city"),
          row.getAs[Long]("times"),
          row.getAs[Int]("times_rank")))
      })
      StatDao.saveCityVideoTopNToMysql(list)
    } catch {
      case e: Exception => e.printStackTrace()
      case _ => println("unknown error!please check!")
    }
  }

  /**
    * 按某一天流量进行统计topN   day,cmsId,traffic
    */
  def videoTrafficTopN(spark: SparkSession, day: String, accessDF: DataFrame): Unit = {

    import spark.implicits._
    val trafficDF = accessDF.filter($"day" === day && $"cmsType" === "video")
      .groupBy("day", "cmsId")
      .agg(sum("traffic").as("traffics"))
      .orderBy($"traffics".desc)

    try {
      val list = new ListBuffer[DayVideoAccessTrafficTopNStat]
      trafficDF.collect().foreach(row => {
        list += DayVideoAccessTrafficTopNStat(
          row.getAs[String]("day"),
          row.getAs[Long]("cmsId"),
          row.getAs[Long]("traffics")
        )
      })
      StatDao.saveDayVideoAccessTraffic(list)

    } catch {
      case e: Exception => e.printStackTrace()
    }
  }


}
