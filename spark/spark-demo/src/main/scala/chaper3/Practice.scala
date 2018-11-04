package chaper3

import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/22 17:11
  */
object Practice {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
    conf.setAppName("task")
    conf.setMaster("local")
    val sc = new SparkContext(conf)

    //统计文本中性别为男的数目
    val sex = sc.textFile("G:\\data\\chapter3\\03-practice\\test.txt")
    val male = sex.filter(_.contains("男")).count()
    println(s"男人的数目:$male")


    val rdd1 = sc.parallelize(List(('a',1),('b',1)),1)
    val rdd2 = sc.parallelize(List(('a',1),('c',1)),1)
//    rdd1.leftOuterJoin(rdd2).foreach(println)

    //只有相同的数目和相同的分区数才可以进行zip操作
    val rdd3 = sc.makeRDD(1 to 5,2)
    val rdd4 = sc.makeRDD(1 to 10,2)
//    rdd3.zip(rdd4).foreach(println)

    println("partitioner:"+rdd1.partitioner)

  }
}
