package mooc

import org.apache.spark.sql.SparkSession

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/28 11:23
  */
object JsonDemo {

  /*+--------------------+----+---------+-------+
    |             address| age| luckyNum|   name|
    +--------------------+----+---------+-------+
    |[shenzhen,guangdong]|null|[1, 2, 3]|Michael|
    |[guangzhou,guangd...|  30|   [4, 5]|   Andy|
    |    [changsha,hunan]|  19|     null| Justin|
    +--------------------+----+---------+-------+
    */


  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .master("local").appName("json")
      .getOrCreate()
    val jsonDF = spark.read.json("file:///G:\\data\\第5章\\第5章\\people.json")

    jsonDF.show()
    jsonDF.createOrReplaceTempView("jsonTable")

    //通过 . 访问内嵌的信息
    spark.sql("select name,address.city,luckyNum[1] from jsonTable ").show()

    //使用explode()函数将数据展开(针对数据，列表)
    spark.sql("select name,explode(luckyNum) from jsonTable").show()

  }
}
