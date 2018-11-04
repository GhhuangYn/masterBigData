package chaper3

import java.io.StringReader

import com.opencsv.CSVReader
import org.apache.spark.{SparkConf, SparkContext}


/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/22 13:05
  */
object Demo1 {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName("task")
    conf.setMaster("local")
    val sc = new SparkContext(conf)

    val bigdata = sc.textFile("G:\\data\\chapter3\\01-task\\data\\result_bigdata.txt")
    val math = sc.textFile("G:\\data\\chapter3\\01-task\\data\\result_math.txt")
    bigdata.map(line => {
      val arr = line.split("\t")
      (arr(0), arr(1), arr(2).toInt)
    }).sortBy(_._3, ascending = false)
      .take(5)
      .foreach(println)

    val rdd2 = bigdata.map(line => {
      val arr = line.split("\t")
      (arr(0), arr(1), arr(2).toInt)
    })
    val rdd3 = math.map(line => {
      val arr = line.split("\t")
      (arr(0), arr(1), arr(2).toInt)
    })
    //100分学生的id
    //    rdd2.union(rdd3).filter(_._3 == 100).foreach(s => println(s._1))

    //总分
    val totalScore = rdd2.union(rdd3).map(x => (x._1, x._3)).reduceByKey(_ + _)

    val bigdata2 = bigdata.map(line => {
      val arr = line.split("\t")
      (arr(0), arr(2).toInt)
    })
    val math2 = math.map(line => {
      val arr = line.split("\t")
      (arr(0), arr(2).toInt)
    })

    //平均成绩
    /*
      def combineByKey[C](
            createCombiner: V => C,           //V是RDD的value部分,C是需要转换成另外一种类型的累加器初值(此方法遇到新键时调用)
            mergeValue: (C, V) => C,          //把V合并到前一个C上(非新键调用，此操作在每个分区中进行)
            mergeCombiners: (C, C) => C)      //把函数的两个C合并(如果不同分区有相同的键则需要调用这个方法)
            : RDD[(K, C)]
    }*/
    val avg_score = bigdata2.union(math2).combineByKey(
      score => (score, 1), //(分数,1)
      (t: (Int, Int), score: Int) => (t._1 + score, t._2 + 1),
      (t1: (Int, Int), t2: (Int, Int)) => (t1._1 + t2._1, t1._2 + t2._2)
    ).map(t => (t._1, t._2._1 / t._2._2))

    //读取CSV文件
    val csv = sc.textFile("G:\\data\\chapter3\\01-task\\data\\testcsv.csv")
    val result = csv.map(line => {
      val reader = new CSVReader(new StringReader(line))
      reader.readNext()
    }) //返回的RDD包含一个二维数组
    result.foreach(x => println(x.mkString(",")))


    /*
        任务实现：连接学生表、大数据基础成绩表、数学成绩表
     */
    val student = sc.textFile("G:\\data\\chapter3\\01-task\\data\\student.txt", 1)
    student.map(x => {
      val line = x.split("\t")
      (line(0), line(1))
    })
      .join(bigdata2.join(math2))
      .join(totalScore.join(avg_score))
      .map(x => (x._1, x._2._1._1, x._2._1._2._1, x._2._1._2._2, x._2._2._1, x._2._2._2))
      .saveAsTextFile("G:\\data\\chapter3\\01-task\\data\\total")


  }

}
