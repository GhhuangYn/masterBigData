package mooc.log

import java.sql.Connection

import mooc.log.dao.StatDao
import mooc.log.util.MysqlUtil
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import scala.collection.mutable.ListBuffer

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/26 21:41
  */
object TopNStatJob {


  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .config("spark.sql.sources.partitionColumnTypeInference.enabled", value = false) //关闭自动推倒数据类型
      .master("local[2]").appName("TopNStatJob")
      .getOrCreate()
    import spark.implicits._

    val data = spark.read.parquet("G:\\access")
    data.printSchema()
    //data.show()


    //每次测试先删除数据库中的所有数据
    val day = "20170718"
    StatDao.deleteData(day)

    //优化1 把公用的DF提取出来
    val commonDF = data.filter($"day" === day && $"cmsType" === "video")

    //优化2 把commonDF缓存下来
    commonDF.cache()

    //最受欢迎的视频topN课程(某一天)
    videoAccessTopNStat(spark, commonDF)

    //按照地市进行统计topN课程(按城市统计)
    videoAccessTopNStatOfCity(spark, commonDF)

    //按照流量进行统计
    videoTrafficTopN(spark, commonDF)

    //清除缓存的数据
    commonDF.unpersist()

    spark.stop()

  }

  //先统计，再保存到数据库

  /**
    * 最受欢迎的视频topN课程(某一天)
    */

  import org.apache.spark.sql.functions._

  def videoAccessTopNStat(spark: SparkSession, commonDF: DataFrame): Unit = {

    import spark.implicits._
    val videoAccessTopN = commonDF.groupBy("day", "cmsId")
      .agg(count("cmsId").as("times"))
      .orderBy($"times".desc)
    videoAccessTopN.show(false)

    //使用sql
    /*accessDF.createOrReplaceTempView("videoRecord")
    val result = spark.sql("select day,cmsId ,count(*) times from videoRecord " +
      s"where day='$day' and cmsType='video' group by day,cmsId order by times desc")
    result.show(false)*/

    //保存到mysql中
    StatDao.saveDayVideoAccessTopNStatToMysql(videoAccessTopN.collect())

  }


  /**
    * 最受欢迎的视频topN课程,按照某一天，某个城市统计
    */
  def videoAccessTopNStatOfCity(spark: SparkSession, commonDF: DataFrame): Unit = {
    import spark.implicits._
    val cityTopNDF = commonDF.groupBy("day", "city", "cmsId")
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
  def videoTrafficTopN(spark: SparkSession, commonDF: DataFrame): Unit = {

    import spark.implicits._
    val trafficDF = commonDF.groupBy("day", "cmsId")
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
