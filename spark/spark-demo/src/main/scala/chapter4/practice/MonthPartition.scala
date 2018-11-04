package chapter4.practice

import org.apache.spark.Partitioner

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/23 9:00
  */
class MonthPartition(numParts: Int) extends Partitioner {

  override def numPartitions: Int = numParts

  override def getPartition(date: Any): Int = date.toString.substring(5, 7) match {
    case "03" => 0
    case "04" => 1
    case _ => 2
  }

  override def equals(obj: scala.Any): Boolean = obj match {
    case obj: MonthPartition => obj.numPartitions == this.numParts
    case _ => false
  }

}
