package chapter4

import org.apache.spark.Partitioner

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/22 19:13
  */
class MyPartitioner(numParts: Int) extends Partitioner {

  override def numPartitions: Int = numParts

  override def getPartition(key: Any): Int = {
    if (key.toString.toInt % 2 == 0) {
      0
    } else 1
  }

  override def equals(ohter: scala.Any): Boolean = {
    ohter match {
      case mypartition: MyPartitioner => mypartition.numPartitions == numPartitions
      case _ => false
    }
  }
}
