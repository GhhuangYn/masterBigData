package mooc.log.dao

import java.sql.PreparedStatement

import mooc.log.{DayVideoAccessTrafficTopNStat, DayVideoCityAccessTopNStat}
import mooc.log.util.MysqlUtil
import org.apache.spark.sql.Row

import scala.collection.mutable.ListBuffer

/**
  * @Description: 把统计的信息写入数据库的DAO
  * @author: HuangYn 
  * @date: 2018/10/27 11:03
  */
object StatDao {

  //根据流量统计每天每门课的 TOPN
  def saveDayVideoAccessTraffic(list: ListBuffer[DayVideoAccessTrafficTopNStat]): Unit = {

    val connection = MysqlUtil.Connection()
    val pst = connection.prepareStatement("insert into day_video_traffic_access_topn_stat values(?,?,?)")
    for (elem <- list) {
      pst.setString(1, elem.day)
      pst.setLong(2, elem.cmsId)
      pst.setLong(3, elem.traffics)
      pst.addBatch()
    }
    pst.executeBatch()
    MysqlUtil.release(connection, pst)
  }

  //按地市统计每天的TOPN课程
  def saveCityVideoTopNToMysql(data: ListBuffer[DayVideoCityAccessTopNStat]): Unit = {
    val connection = MysqlUtil.Connection()
    val pst = connection.prepareStatement("insert into day_video_city_access_topn_stat values(?,?,?,?,?)")
    for (elem <- data) {
      pst.setString(1, elem.day)
      pst.setString(2, elem.city)
      pst.setLong(3, elem.cmsId)
      pst.setLong(4, elem.times)
      pst.setInt(5, elem.timesRank)
      pst.addBatch()
    }
    pst.executeBatch()
    MysqlUtil.release(connection, pst)
  }

  //最受欢迎的视频topN课程(某一天)
  def saveDayVideoAccessTopNStatToMysql(result: Array[Row]): Unit = {

    val connection = MysqlUtil.Connection()
    val pst = connection.prepareStatement("insert into day_video_access_topn_stat values(?,?,?)")
    result.foreach(row => {
      pst.setString(1, row.getAs[String]("day"))
      pst.setLong(2, row.getAs[Long]("cmsId"))
      pst.setLong(3, row.getAs[Long]("times"))
      pst.addBatch()
    })
    pst.executeBatch()
    MysqlUtil.release(connection, pst)
  }

  def deleteData(day: String): Unit = {
    val tables = Array[String](
      "day_video_access_topn_stat",
      "day_video_city_access_topn_stat",
      "day_video_traffic_access_topn_stat")

    val connection = MysqlUtil.Connection()
    var pst:PreparedStatement = null
    try {
      for (table <- tables) {
        pst = connection.prepareStatement(s"truncate $table")
        pst.execute()
      }
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      MysqlUtil.release(connection, pst)
    }


  }
}
