package mooc

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.internal.SQLConf

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/25 17:06
  */
object HiveContextDemo {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .enableHiveSupport() //开启hive支持
      .getOrCreate()

    spark.sql("use rel")
    spark.table("student_info").show()

    val dfPeople = spark.read.format("json").load("G:\\data\\第5章\\第5章\\people.json")
    dfPeople.write.saveAsTable("people") //可以保存到hive中

  }
}
