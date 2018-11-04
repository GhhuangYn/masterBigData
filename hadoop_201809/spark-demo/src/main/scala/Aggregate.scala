import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/18 9:52
  */
object Aggregate {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setMaster("local")
    conf.setAppName("group")
    val data = List((1, 3), (1, 2), (1, 4), (2, 3))
    val sc = new SparkContext(conf)
    val rdd1 = sc.parallelize(data, 2)

    /*
    *  对PairRDD中相同的Key值进行聚合操作，在聚合过程中同样使用了一个中立的初始值。
    *  和aggregate函数类似，aggregateByKey返回值的类型不需要和RDD中value的类型一致。
    *  因为aggregateByKey是对相同Key中的值进行聚合操作，所以aggregateByKey'函数最终返回的类型还是PairRDD，
    *  对应的结果是Key和聚合后的值，而aggregate函数直接返回的是非RDD的结果。
    *
    *  它是先对每个<<分区>>中相同key的value进行聚合，然后再对所有的分区相同的key进行聚合
    */

    rdd1.aggregateByKey("100")((a: String, b: Int) => {
      println(s" SeqOp: a=$a , b=$b")
      a + b
    }, (a: String, b: String) => {
      println(s" comOp: a=$a , b=$b")
      a + b
    }).foreach(println)

    /**
      * seqOp的操作是遍历分区中的所有元素(T)，第一个T跟zeroValue做操作，
      * 结果再作为与第二个T做操作的zeroValue，直到遍历完整个分区
      *
      */
    val rdd2 = List(1, 2, 3, 4, 5, 6, 7, 8, 9)

    val result = rdd2.aggregate(100)((a: Int, b: Int) => {
      println(s"seqOp: a=$a,b=$b")
      a + b
    }, (a: Int, b: Int) => {
      println(s"comOp: a=$a,b=$b")
      a + b
    })
    println(s"result=$result")


  }
}
