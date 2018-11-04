package mooc

import org.apache.spark.sql.SparkSession

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/25 18:40
  */
object SparkSessionDemo {

  def main(args: Array[String]): Unit = {

    //since spark 2.0
    val spark = SparkSession
      .builder()
      .master("local")
      .appName("sparkSessionApp")
      .getOrCreate()
    val people = spark.read.json("G:\\data\\第5章\\第5章\\people.json")
    people.show()
    spark.close()

  }
}
