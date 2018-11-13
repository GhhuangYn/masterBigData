package project.tag

import com.fasterxml.jackson.databind.util.JSONPObject
import net.sf.json.{JSONArray, JSONObject, JSONString}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/11/13 14:58
  */
object TagStatApp {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("tag").setMaster("local[2]")
    val sc = new SparkContext(conf)

    sc.textFile("G:\\temptags.txt")
      .map(line => {
        val arr = line.split("\t")
        (arr(0), arr(1))
      })
      .filter(t => {
        val json = JSONObject.fromObject(t._2)
        !json.get("extInfoList").equals("null")
      })
      .filter(t => {
        val json = JSONObject.fromObject(t._2)
        val extInfoListJson = JSONArray.fromObject(json.get("extInfoList"))
        extInfoListJson.size() > 0
      })
      .map(t => {
        val json = JSONObject.fromObject(t._2)
        val extInfoListJson = JSONArray.fromObject(json.get("extInfoList"))
        val comments = JSONObject.fromObject(extInfoListJson.get(0)).get("values")
        (t._1, comments.toString.replace("[", "").replace("]", ""))
      })
      .map(t => t._1 -> t._2.split(","))
      .flatMapValues(e => e)
      .map(t => ((t._1, t._2), 1))
      .reduceByKey(_ + _)
      .map(t => t._1._1 -> List((t._1._2, t._2)))
      .reduceByKey(_ ::: _)
      .map(t => (t._1, t._2.sortWith((t1, t2) => t1._2 > t2._2)))

      .foreach(println)

  }

}
