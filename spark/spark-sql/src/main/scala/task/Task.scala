package task

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.hive.HiveContext

/**
  * @Description: 第五章，任务5.3
  * @author: HuangYn 
  * @date: 2018/10/24 9:25
  */
object Task {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local").setAppName("website")
    val sparkContext = new SparkContext(sparkConf)
    //    val builder = SparkSession.builder.enableHiveSupport
    //    val sparkSession = builder.config(sparkConf).getOrCreate()
    val hiveContext = new HiveContext(sparkContext)
    hiveContext.sql("use law")

    //网页类型分析
    hiveContext.sql(
      """
        | select substring(page_type,1,3) as page_type,
        |       count(*) as count_num, count_num/837450.0 as weights
        | group by page_type
        | from law
        | order by weights desc
        | limit 7
      """)
  }
}
