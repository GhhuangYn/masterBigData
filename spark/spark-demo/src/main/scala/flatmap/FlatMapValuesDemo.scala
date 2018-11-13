package flatmap

import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/11/13 17:33
  */
object FlatMapValuesDemo {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("tag").setMaster("local[2]")
    val sc = new SparkContext(conf)
    val a = sc.parallelize(List("fruit" -> "apple,banana,pear", "animal" -> "pig,cat,dog,tiger"))

    //flatMapValues():对于key->(v1,v2,..)的列表,把values进行map操作，然后key对v进行一一映射，再flat展平
    a.flatMapValues(_.split(",")).foreach(println)

    //mapValues(): 对value进行map操作
    val b = sc.parallelize(List("dog", "tiger", "lion", "cat", "panther", "eagle"), 2)
    b.map(x => (x.length, x)).mapValues("!" + _ + "!").foreach(println)

  }

}
