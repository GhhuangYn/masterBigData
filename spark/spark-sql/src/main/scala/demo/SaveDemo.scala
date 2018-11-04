package demo

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SQLContext, SaveMode}

/**
  * @Description: 写入文件
  * @author: HuangYn 
  * @date: 2018/10/23 20:35
  */

object SaveDemo {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local").setAppName("sql")
    val sparkContext = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sparkContext)

    val jdbcDF = sqlContext.read.format("jdbc")
      .options(Map(
        "url" -> "jdbc:mysql://localhost:3306/spring",
        "driver" -> "com.mysql.jdbc.Driver",
        "user" -> "root",
        "password" -> "90741xx",
        "dbtable" -> "person")).load()

    val data = jdbcDF.select("name", "age", "address")
    data.write.partitionBy("age").json("agePart.json") //按照age进行分区保存

    /*注意:partition是按照column进行分区的
     而bucket是按照row进行分区的(只有保存成table才能使用bucket分桶)
     在使用spark sql重新读取保存的文件时，只需要整个文件夹读取，spark sql会自动解析出partition的字段信息
     */

    /*
      SaveMode.ErrorIfExists (default)
      SaveMode.Append
      SaveMode.Overwrite
      SaveMode.Ignore
    */
    //    data.write.format("json")
    //      .mode(SaveMode.Append)
    //      .options(Map("header" -> "true", "path" -> "G:\\data\\第5章\\第5章\\a.txt"))
    //      .save()


    //    data.write.mode(SaveMode.Append)
    //      .options(Map("header" -> "true", "path" -> "G:\\data\\第5章\\第5章\\a.txt"))
    //      .saveAsTable("person")

  }
}
