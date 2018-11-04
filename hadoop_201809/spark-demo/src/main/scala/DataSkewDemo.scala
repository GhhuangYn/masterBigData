import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Random

/**
  * @Description: 数据倾斜问题
  * @author: HuangYn 
  * @date: 2018/10/18 12:54
  */
object DataSkewDemo {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setMaster("local")
    conf.setAppName("group")
    val sc = new SparkContext(conf)
    val rdd1 = sc.textFile("G:\\wc.txt", 3)
    val rdd2 = rdd1.flatMap(_.split(" ")).map((_, 1)).map(t => {
      val r = Random.nextInt(4)
      (t._1 + "_" + r, t._2) //通过对key进行变换，是的key随机分配到每个partition中
    })

    //其实现在已经可以做到均匀分配，不需要手动解决数据倾斜
    //    rdd2.mapPartitionsWithIndex((i, it) => {
    //      it.foreach(t=>println(s"partition: $i ==> $t"))
    //      it
    //    }).foreach(println)

    rdd2.reduceByKey(_ + _).map(t=>{
      val w = t._1.split("_")(0)
      (w,t._2)
    }).reduceByKey(_+_).foreach(println)

  }

}
