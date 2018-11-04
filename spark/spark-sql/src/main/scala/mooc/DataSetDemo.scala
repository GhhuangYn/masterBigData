package mooc

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.hive.HiveContext

/**
  * @Description: DataSet创建
  * @author: HuangYn 
  * @date: 2018/10/25 22:45
  */
object DataSetDemo {

  case class Person(name: String, age: Long)

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .master("local[2]")
      .appName("dataSet")
      .config("spark.sql.shuffle.partitions",10)
      .getOrCreate()


    import spark.implicits._

    //DataSet使用Encoder序列化对象
    val caseClassDS = Seq(Person("Andy", 32), Person("Leo", 12)).toDS()
    caseClassDS.write.format("json").save("G:\\data\\第5章\\第5章\\people3.json")
    caseClassDS.show()
    // Encoders for most common types are automatically provided by importing spark.implicits._
    val primitiveDS = Seq(1, 2, 3).toDS()
    primitiveDS.map(_ * 10).show()

    val primitiveDF = primitiveDS.toDF("num")   //dataSet->dataFrame
    primitiveDF.show()

    // DataFrames can be converted to a Dataset by providing a class. Mapping will be done by name
    val dfPeople = spark.sqlContext.read.format("json").load("G:\\data\\第5章\\第5章\\people.json")
    val peopleDS = dfPeople.as[Person]
    peopleDS.show()

  }

}
