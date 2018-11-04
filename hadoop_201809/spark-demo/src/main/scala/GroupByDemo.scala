import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/17 21:59
  */
object GroupByDemo {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setMaster("local")
    conf.setAppName("group")
    val sc = new SparkContext(conf)
    val rdd1 = sc.textFile("G:\\hello.txt", 2)
    rdd1.mapPartitions(it => {
      println(it.next());
      it
    })
    val rdd2 = rdd1.map(line => {
      val city = line.split(" ")(2)
      (city, line)
    })

    //1.使用groupByKey()分组，sortByKey()排序
    val rdd3 = rdd2.groupByKey()
    //    rdd3.foreach((r) => {
    //      println(s"------city: ${r._1} -------")
    //      r._2.foreach(println)
    //    })

    //2.使用sortBy
    //    rdd3.sortByKey(ascending = true, 4).foreach(println)

    //3.join(otherDataset, [numTasks])			//连接,(K,V).join(K,W) =>(K,(V,W))
    val userRdd = sc.textFile("G:\\user.txt")
    val scoreRdd = sc.textFile("G:\\score.txt")
    val rdd4 = userRdd.map(line => {
      val city = line.split(" ")(2)
      val key = line.substring(0, line.lastIndexOf(" "))
      (key, city)
    })
    val rdd5 = scoreRdd.map(line => {
      val score = line.split(" ")(2)
      val key = line.substring(0, line.lastIndexOf(" "))
      (key, score)
    })
//    rdd4.join(rdd5, 3).foreach(println)

    //4.cogroup()协分组  (K,V).cogroup(K,W) =>(K,(Iterable<V>,Iterable<!-- <W> -->))

    //5.笛卡儿积 RR[T] RDD[U] => RDD[(T,U)]  所有组合都组合一遍
    val rdd6 = sc.parallelize(Array("leo", "bobby", "jack"))
    val rdd7 = sc.parallelize(Array(1, 2, 3, 4),2)
//    rdd6.cartesian(rdd7).foreach(println)

    //6.保存到文件
    rdd6.cartesian(rdd7).saveAsTextFile("G:\\cartesian")

  }
}
