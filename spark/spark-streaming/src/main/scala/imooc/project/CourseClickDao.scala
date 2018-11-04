package imooc.project

import imooc.hbase1.HBaseUtils
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.util.Bytes

import scala.collection.mutable.ListBuffer

/**
  * @Description: 保存统计数据到Hbase的DAO
  * @author: HuangYn 
  * @date: 2018/11/1 20:45
  */
object CourseClickDao {

  val tableName = "imooc_course_clickcount"
  val cf = "info"
  val qualifier = "click_count"

  //保存每天到当前时间为止,课程的点击量
  def saveCourseClickCount(list: ListBuffer[CourseClickCount]): Unit = {
    if (list.nonEmpty && list.size > 1) {

      val table = HBaseUtils.getInstance.getTable("imooc_course_clickcount")
      list.foreach(ccc => {
        table.incrementColumnValue(
          ccc.day_course.getBytes(),
          cf.getBytes(),
          qualifier.getBytes(),
          ccc.click_count)
      })
    }
  }

  //保存每天到当前时间为止,从搜索引擎搜索进入课程的点击量
  def saveCourseSearchClickCount(list: ListBuffer[CourseSearchClickCount]): Unit = {
    if (list.nonEmpty && list.size > 1) {
      val table = HBaseUtils.getInstance().getTable("imooc_course_search_clickcount")
      list.foreach(c =>
        table.incrementColumnValue(Bytes.toBytes(c.day_from),
          Bytes.toBytes(cf),
          Bytes.toBytes(qualifier),
          c.click_count)
      )
    }
  }

  //查询数据,每天到当前时间为止,课程的点击量
  def getCourseClickCount(day: String, course: String): CourseClickCount = {
    val table = HBaseUtils.getInstance().getTable("imooc_course_clickcount")
    val result = table.get(new Get((day + "_" + course).getBytes()))
    val cell = result.getColumnCells(cf.getBytes(), qualifier.getBytes()).get(0)
    CourseClickCount(
      new String(cell.getRowArray, cell.getRowOffset, cell.getRowLength),
      Bytes.toLong(cell.getValueArray, cell.getValueOffset, cell.getValueLength)
    )
  }

  def getCourseSearchClickCount(day: String, from: String): CourseSearchClickCount = {
    val table = HBaseUtils.getInstance().getTable("imooc_course_search_clickcount")
    val result = table.get(new Get((day + "_" + from).getBytes()))
    val cell = result.getColumnCells(cf.getBytes(), qualifier.getBytes()).get(0)
    CourseSearchClickCount(
      new String(cell.getRowArray, cell.getRowOffset, cell.getRowLength),
      Bytes.toLong(cell.getValueArray, cell.getValueOffset, cell.getValueLength)
    )
  }


  def main(args: Array[String]): Unit = {

    //    val list = ListBuffer(CourseClickCount("20181111_666", 10))
    //    save(list)
    val ccc = getCourseClickCount("20181111", "666")
    print(s"${ccc.day_course} : ${ccc.click_count}")
  }

}
