package bigadta.spark.sql.test

import java.util.Properties

import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, Row, SparkSession}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

/**
 * Created by xiaoguanyu on 2018/1/19.
 */
object DFTest {
  case class UserCoreDF(userId : String,age : Int,gender : String,core : Int)
  def main(args: Array[String]) {
    val conf = new SparkConf()
    //创建sparksession
    val spark = SparkSession.builder()
                            .master("local[2]")
                            .appName("dftest")
                            .config(conf)
                            .getOrCreate()
    val sc = spark.sparkContext
    //RDD -> DataFrame 通过反射
    val userCoresRdd = sc.textFile("hdfs://192.168.183.100:9000/data/sparksql/user")
    val userCoreRdd = userCoresRdd.map(_.split("\t")).map(cols => UserCoreDF(cols(0),cols(1).toInt,cols(2),cols(3).toInt))
    //引入隐式转换，将RDD隐式转换成DataFrame
    import spark.implicits._
    val userCoreDF = userCoreRdd.toDF()
//    userCoreDF.take(2).foreach(println(_))
//    println("userCoreDF count ----------->" + userCoreDF.count())

    //RDD -> DataFrame 显示定义schema
    //定义schema
    val userCoreSchema = StructType(
      List(
        StructField("userId",StringType,true),
        StructField("age",IntegerType,true),
        StructField("gender",StringType,true),
        StructField("core",IntegerType,true)
      )
    )
    val userCoreRdd2 = userCoresRdd.map(_.split("\t")).map(cols => Row(cols(0),cols(1).toInt,cols(2),cols(3).toInt))
    val userCoreDF2 = spark.createDataFrame(userCoreRdd2,userCoreSchema)
    //println("userCoreDF2 count ----------->" + userCoreDF2.count())
/*

    //dataframe -> hdfs json文件
    userCoreDF2.write.mode(SaveMode.Overwrite).json("hdfs://192.168.183.100:9000/tmp/use_json")
    //dataframe -> hdfs parquet文件
    userCoreDF2.write.mode(SaveMode.Overwrite).parquet("hdfs://192.168.183.100:9000/tmp/use_parquet")
*/
    //通过内置API读取指定格式文件，创建DataFrame
//    val jsonDF1 = spark.read.format("json").load("hdfs://192.168.183.100:9000/tmp/use_json")
//    println("jsonDF1 count ----------->" + jsonDF1.count())
    val jsonDF2 = spark.read.json("hdfs://192.168.183.100:9000/tmp/use_json")
    /*println("jsonDF2 count ----------->" + jsonDF2.count())
    println("jsonDF2 count ----------->" + jsonDF2.schema)*/


    //通过JDBC读取mysql 创建dataframe
    /*val connconf = new Properties()
    connconf.put("user","hive")
    connconf.put("password","hive123")
    val jdbcdf = spark.read.jdbc("jdbc:mysql://192.168.183.101:3306","hive.TBLS",connconf)
    jdbcdf.show()*/

    //通过DSF
    //jsonDF2.filter("age > 20").select("gender","core").groupBy("gender").sum("core").show()

    jsonDF2.createOrReplaceTempView("user_core")

    spark.sql("select gender,sum(core) from user_core where age>20 group by gender").show()




  }
}
