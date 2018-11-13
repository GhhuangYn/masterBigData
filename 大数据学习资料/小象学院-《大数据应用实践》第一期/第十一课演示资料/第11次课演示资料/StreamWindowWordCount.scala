package bigadta.spark.streamtest

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * 基于window
  * Created by xiaoguanyu on 2018/1/23.
  */
object StreamWindowWordCount {
   def main(args: Array[String]) {
     val conf = new SparkConf().setAppName("streamwordcount").setMaster("local[2]")
     //创建StreamingContext，需要设置时间间隔
     val ssc = new StreamingContext(conf,Seconds(5))
     //创建初始dstream
     val ds =ssc.socketTextStream("192.168.183.100",8888)
     val rsDs = ds.flatMap(_.split(" ")).map((_,1))
       .reduceByKeyAndWindow((x:Int,y:Int) => x + y,Seconds(30),Seconds(10))
     rsDs.print()
     ssc.start()
     ssc.awaitTermination()
   }
 }
