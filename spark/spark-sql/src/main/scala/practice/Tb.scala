package practice

import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SQLContext, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/24 21:29
  */
object Tb {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("tb").setMaster("local[8]")
    val sparkContext = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sparkContext)
    val dateRDD = sparkContext.textFile("G:\\data\\第5章\\第5章\\03-实训数据\\tbDate.txt")
    val stockRDD = sparkContext.textFile("G:\\data\\第5章\\第5章\\03-实训数据\\tbStock.txt")
    val stockDetailRDD = sparkContext.textFile("G:\\data\\第5章\\第5章\\03-实训数据\\tbStockDetail.txt")


    //构建命名空间
    val dateSchemaString = "Dateid Theyearmonth Theyear Themonth Thedate Theweek theweeks thequot thetenday thehalfmonth"
    val stockSchemaString = "Ordernumber Locationid DateID"
    val stockDetailSchemaString = "Ordernumber Rownum Itemid Qty Price Amount"

    val dateSchema = StructType(
      dateSchemaString.split(" ").map(field => StructField(field, StringType, nullable = false)))
    val dateRDD2 = dateRDD.map(_.split(",")).map(d => Row(d(0), d(1), d(2), d(3), d(4), d(5), d(6), d(7), d(8), d(9)))
    val dateDF = sqlContext.createDataFrame(dateRDD2, dateSchema)

    val stockSchema = StructType(
      stockSchemaString.split(" ").map(field => StructField(field, StringType, nullable = false))) //构建Filed的schema
    val stockRDD2 = stockRDD.map(_.split(",")).map(s => Row(s(0), s(1), s(2))) //把原始数据映射成ROW
    val stockDF = sqlContext.createDataFrame(stockRDD2, stockSchema) //把row和field组合

    val stockDetailSchema = StructType(
      stockDetailSchemaString.split(" ").map(field => StructField(field, StringType, nullable = false)))
    val stockDetailRDD2 = stockDetailRDD.map(_.split(",")).map(d => Row(d(0), d(1), d(2), d(3), d(4), d(5)))
    val stockDetailDF = sqlContext.createDataFrame(stockDetailRDD2, stockDetailSchema)

    dateDF.createOrReplaceTempView("tbDate")
    stockDF.createOrReplaceTempView("tbStock")
    stockDetailDF.createOrReplaceTempView("tbStockDetail")

    /* val result1 = sqlContext.sql(
       " select substring(a.DateID,1,4) year,sum(b.Price * b.Amount) amount " +
         " from tbStock a left join tbStockDetail b " +
         " on a.Ordernumber = b.Ordernumber " +
         " group by substring(a.DateID,1,4) order by amount desc"
     )
     val result2 = sqlContext.sql(
       " select substring(a.DateID,1,4) year,count(*) " +
         " from tbStock a group by substring(a.DateID,1,4)"
     )

     val result3 = result1.join(result2, List("year"))
     result3.orderBy(-result3("amount")).show()*/

    val orderSum = sqlContext.sql("select t1.Ordernumber, sum(t1.Price*t1.Amount) sum from tbStockDetail t1 group by t1.Ordernumber")
    val yeargroup = sqlContext.sql("select a.Ordernumber,substring(a.DateID,1,4) DateID from tbStock a ")
    orderSum.join(yeargroup, "Ordernumber").groupBy("DateID").max("sum").orderBy("max(sum)").show(20)

  }

}
