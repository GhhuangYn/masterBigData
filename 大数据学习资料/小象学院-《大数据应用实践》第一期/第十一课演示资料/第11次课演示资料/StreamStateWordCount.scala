package bigadta.spark.streamtest

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{StateSpec, State, Seconds, StreamingContext}

/**
 * mapWithState
  * Created by xiaoguanyu on 2018/1/23.
  */
object StreamStateWordCount {
   def main(args: Array[String]) {
     val conf = new SparkConf().setAppName("streamwordcount").setMaster("local[2]")
     //创建StreamingContext，需要设置时间间隔
     val ssc = new StreamingContext(conf,Seconds(5))
     ssc.checkpoint("hdfs://192.168.183.100:9000/tmp/stateck")
     //创建初始dstream
     val ds =ssc.socketTextStream("192.168.183.100",8888)
     val rsDs = ds.flatMap(_.split(" ")).map((_,1)).reduceByKey(_ + _)

     val mappingFunc = (word : String,one : Option[Int],state : State[Int]) => {
       val sum = one.getOrElse(0) + state.getOption().getOrElse(0)
       val output = (word,sum)
       state.update(sum)
       output
     }
     val rs = rsDs.mapWithState(StateSpec.function(mappingFunc))
     rs.print()
     ssc.start()
     ssc.awaitTermination()
   }
 }
