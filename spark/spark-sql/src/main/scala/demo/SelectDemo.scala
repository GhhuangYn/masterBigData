package demo

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql._
import org.apache.spark.sql.internal.SQLConf
import org.apache.spark.sql.types.{StringType, StructField, StructType}

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/23 19:52
  */
object SelectDemo {
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

    //DATAFRAME的查询方法
    //方法1 DataFrame注册成临时表，然后通过sql查询
    jdbcDF.createOrReplaceTempView("personTmpTable")
    val personsDF = sqlContext.sql("select id,name,age,address from personTmpTable where age < 29 ")
    personsDF.show()

    //方法2 直接在DATAFrame中进行查询
    jdbcDF.where("name = 'xx' and address = '北京' ").show()
    jdbcDF.filter(row => row.getAs("name").toString.length > 2).show()

    //获取指定字段
    jdbcDF.select("name", "address", "age").show()

    //注册自定义函数
    sqlContext.udf.register("replace", (x: Int) => {
      if (x < 30) "young"
      else "old"
    })
    //selectExpr()对指定字段进行特殊的处理
    jdbcDF.selectExpr("name", "replace(age) as age", "address").show()

    //col() / apply()获取一个字段
    val nameCol = jdbcDF.col("name")
    println(nameCol) //name#3

    //limit()  sortBy
    jdbcDF.select(new Column("age"), jdbcDF("name").as("年龄"))
      .sort(new Column("age").desc, new Column("name").asc).show()

    //spark2.0 使用sparkSession
    val spark = SparkSession.builder().master("local").appName("test").getOrCreate()
    import spark.implicits._
    jdbcDF.select("name", "age").orderBy($"age".desc).show()

    //直接使用SQL操作文件
    val sqlDF = spark.sql("select * from parquet. `G:\\data\\第5章\\第5章\\users.parquet` ")
    sqlDF.printSchema()
    sqlDF.show()

  }
}
