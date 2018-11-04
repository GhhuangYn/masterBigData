package demo

import org.apache.spark.sql.internal.SQLConf
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/23 17:10
  */


object CreateDemo {

  case class Person(name: String, age: Int)

  def main(args: Array[String]): Unit = {
    val sqlConf = new SQLConf()
    val sparkConf = new SparkConf().setMaster("local").setAppName("sql")
    val sparkContext = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sparkContext)

    //1.从结构化文件中创建dataFrame
    val defUsers = sqlContext.read.load("G:\\data\\第5章\\第5章\\users.parquet")
    //    defUsers.show()

    val dfPeople = sqlContext.read.format("json").load("G:\\data\\第5章\\第5章\\people.json")
    //    dfPeople.show()

    //2.从外部数据库创建dataFrame
    val jdbcDF = sqlContext.read.format("jdbc")
      .options(Map(
        "url" -> "jdbc:mysql://localhost:3306/spring",
        "driver" -> "com.mysql.jdbc.Driver",
        "user" -> "root",
        "password" -> "90741xx",
        "dbtable" -> "blog")).load()
    //    val selectDF = jdbcDF.select(new Column("id"), new Column("content"), new Column("summary"))
    //    selectDF.foreach(r => println(r))

    //=======3. RDD转换成dataFrame ======
    //方式1 定义一个case class (定义在本object之外的地方)

    val spark = SparkSession.builder()
      .master("local").appName("toRDD")
      .getOrCreate()

    // For implicit conversions from RDDs to DataFrames
    import spark.implicits._

    val peopleDF = spark.sparkContext
      .textFile("G:\\data\\第5章\\第5章\\people.txt")
      .map(_.split(","))
      .map(attributes => Person(attributes(0), attributes(1).trim.toInt))
      .toDF()
    peopleDF.createOrReplaceTempView("people")
    spark.sql("select * from people").show()
    peopleDF.map(row => "name: " + row.get(0)).show()
    peopleDF.filter(peopleDF("age") > 19).show()
    spark.stop()

    //方式2 创建schema
    //    val data = sparkContext.textFile("G:\\data\\第5章\\第5章\\people.txt")
    //    val schemaString = "name age"
    //Generate the schema based on the string of schema
    //    val schema = StructType(schemaString.split(" ")
    //      .map(fieldName => StructField(fieldName, StringType, nullable = true)))
    //Convert records of the RDD (people) to Rows
    //    val rowRDD = data.map(_.split(",")).map(p => Row(p(0), p(1).trim))
    //    val peopleDF = sqlContext.createDataFrame(rowRDD, schema)
    //    peopleDF.show(2, truncate = false)    //最多显示两条，设置显示所有字符
    //    peopleDF.printSchema() //打印数据模式
    //    println(peopleDF.collect().mkString("\n"))
    //    peopleDF.take(2)    //去前两条

    //从HIVE中读取数据


  }
}
