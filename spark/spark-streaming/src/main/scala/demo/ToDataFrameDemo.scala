package demo

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @Description: DataFrame and SQL Operations
  * @author: HuangYn 
  * @date: 2018/10/30 13:25
  */
object ToDataFrameDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")
    val ssc = new StreamingContext(conf, Seconds(10)) //批处理的时间间隔为10s
    val lines = ssc.socketTextStream("localhost", 6666)

    lines.flatMap(_.split(" "))
      .foreachRDD(rdd => {
        val spark = SparkSession.builder().config(rdd.sparkContext.getConf).getOrCreate()
        import spark.implicits._
        // Convert RDD[String] to DataFrame
        val wordsDF = rdd.toDF("word")
        wordsDF.createOrReplaceTempView("wordsTable")
        spark.sql("select word,count(*) from wordsTable group by word").show()

      })
    ssc.start()
    ssc.awaitTermination()
  }
}
