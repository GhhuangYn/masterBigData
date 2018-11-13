package bigadta.spark

import org.apache.spark.{SparkContext, SparkConf}
/**
 * Created by xiaoguanyu on 2018/1/15.
 */
object WordCount {
  def main(args: Array[String]) {
    val inPath = args(0)
    val outPath = args(1)
    val sparkConf = new SparkConf().setAppName("SparkWordCount")
    val sc = new SparkContext(sparkConf)
    val rowRdd = sc.textFile(inPath)
    //定义一个全局的计数器，用于记录处理的单词数
    val total_counter = sc.accumulator(0L,"total_counter")
    val resultRdd = rowRdd.flatMap(_.split("\t")).map(x=>{
      //每处理一个单词计数器加1
      total_counter += 1
      (x,1)
    }).reduceByKey(_ + _)
    resultRdd.saveAsTextFile(outPath)
    sc.stop()
  }
}


