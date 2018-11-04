package chapter4.stock

import org.apache.spark.Partitioner

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/22 23:34
  */
class YearPartition(numParts: Int) extends Partitioner {

  override def numPartitions: Int = numParts

  override def getPartition(date: Any): Int = {

    println(s"date: $date")
    date.toString.trim.substring(0, 4) match {
      case "2013" => 0
      case "2014" => 1
      case "2015" => 2
      case _ => 3
    }
  }

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case yearPartition: YearPartition => yearPartition.numPartitions == this.numParts
      case _ => false
    }
  }
}
