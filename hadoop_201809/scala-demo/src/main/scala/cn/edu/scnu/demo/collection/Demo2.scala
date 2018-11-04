package cn.edu.scnu.demo.collection

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.collection.parallel.immutable

/**
  * @Description:
  *
  * scala集合类继承关系
  * Iterable --  Seq  -- IndexedSeq<<trait>>(Vector Range)  List Stream Stack Queue
  * --  Set -- HashSet SortedSet
  * --  Map
  * @author: HuangYn 
  * @date: 2018/10/11 19:41
  */
object Demo2 {

  def main(args: Array[String]): Unit = {

    //练习1
    def indexes(s: String): collection.mutable.Map[Char, String] = {
      var map = collection.mutable.Map[Char, String]()
      s.zipWithIndex.groupBy(t => t._1).foreach(g => {
        val vector = g._2
        var indexes = ArrayBuffer[Int]()
        vector.foreach(v => indexes += v._2)
        map += g._1 -> indexes.mkString("{", ",", "}")
      })
      map
    }

    //    indexes("Mississippi").foreach(println(_))

    //练习1 使用不可变映射
    //要改变不可变映射的值，可以使用 += 或者对一个新变量赋值
    def indexes2(s: String): collection.immutable.Map[Char, String] = {
      var map = collection.immutable.Map[Char, String]()
      s.zipWithIndex.groupBy(t => t._1).foreach(g => {
        val vector = g._2
        var indexes = ArrayBuffer[Int]()
        vector.foreach(v => indexes += v._2)
        map += g._1 -> indexes.mkString("{", ",", "}")
      })
      map
    }
    //    indexes2("Mississippi").foreach(println(_))

    //练习2 从ListBuffer中移除排在偶数位的元素
    //方式1
    val s1 = System.currentTimeMillis()
    val listBuffer = ListBuffer[Int]()
    for (i <- 0 to 1000) listBuffer += i
    for (i <- listBuffer.indices.reverse) {
      if (i % 2 == 0)
        listBuffer.remove(i)
    }
    //    println(listBuffer)
    //    println(System.currentTimeMillis - s1)


    val s2 = System.currentTimeMillis()
    val listBuffer2 = ListBuffer[Int]()
    for (i <- 0 to 1000) listBuffer2 += i
    var tmp = ListBuffer[Int]()
    for (i <- listBuffer2.indices) {
      if (i % 2 != 0)
        tmp += listBuffer2(i)
    }
    //    println(tmp)
    //    println(System.currentTimeMillis - s2)

    //练习3
    def test3(sa: Array[String], sm: collection.immutable.Map[String, Int]): Array[Int] = {
      var result = ArrayBuffer[Int]()
      sa.foreach(s => {
        val option = sm.get(s)
        if (option.isDefined) {
          result += option.get
        }
      })
      result.toArray
    }

    def test33(sa: Array[String], sm: collection.immutable.Map[String, Int]): Array[Int] = {
      sa.flatMap(s => sm.get(s))
    }

    val result = test33(Array("Tom", "Fred", "Harry"),
      collection.immutable.Map("Tom" -> 3, "Dick" -> 4, "Harry" -> 5))
    println(result.mkString(","))

    //练习8
    def trans(col: Int, arr: Array[Double]): Array[Array[Double]] = {
      var tmp = ArrayBuffer[Array[Double]]()
      arr.grouped(col).foreach(r => {
        var tmp2 = ArrayBuffer[Double]()
        r.foreach(e => tmp2 += e)
        tmp += tmp2.toArray
      })
      tmp.toArray
    }

//    Array(1, 2, 3, 4, 5, 6, 9).grouped(3).foreach(u => println(u.mkString(" ")))
    val doubleArr = trans(3,Array(1, 2, 3, 4, 5, 6, 9))
    println(doubleArr.getClass.getTypeName)
    doubleArr.foreach(r=> println(r.mkString(" ")))

  }


}