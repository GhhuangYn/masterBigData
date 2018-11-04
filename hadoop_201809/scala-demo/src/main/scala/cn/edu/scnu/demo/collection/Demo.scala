package cn.edu.scnu.demo.collection

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

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
object Demo {

  def main(args: Array[String]): Unit = {

    //============= List ==============
    //List是不可变的，ListBuffer是可变的
    var digits = List(4, 2)
    println(digits.head)
    println(digits.tail.tail)

    //::操作从给定的头和尾创建一个新的列表  List(9, 7, List(4, 5, 6), List(7))
    var list = 9 :: 7 :: List(4, 5, 6) :: List(7) :: Nil //这样创建出来的列表并不是每个元素都是数字,Nil是一个空的集合
    //    println(List(1, 2, 3, 4).sum)

    //    println(list)

    //============== Set ================
    val set = Set(1, 2, 3, 4, 1, 2)
    //    println(set)

    //将函数映射到集合
    val names = List("Peter", "Paul", "Mary")

    def ulcase(s: String) = Vector(s.toUpperCase(), s.toLowerCase())

    //flatMap
    //    names.map(ulcase).foreach(println(_))
    //    names.flatMap(ulcase) foreach (println(_)) //flatMap会把map之后的所有集合打散，重新组合成一个列表

    //transform
    var buffer = ArrayBuffer("Peter", "Paul", "Mary")
    buffer.transform(_.toLowerCase()) //transform直接改变原数组
    //    println(buffer.mkString)

    //group by
    //    names.groupBy(_.head.toLower).foreach(println) //按照首字母分组

    //对偶的map操作
    val prices = List(5.0, 20.0, 9.95)
    val quantities = List(10, 2, 1)
    val s = (prices zip quantities).map(p => p._1 * p._2).sum
    println(s)

    //zipWithIndex  每个对偶中的组成部分是每个元素的下标
    //    "Scala".zipWithIndex.foreach(u => println(u._1 + ":" + u._2))

    //========= Stream ===========
    def numsFrom(n: BigInt): Stream[BigInt] = n #:: numsFrom(n + 1)

    val tenOrMore = numsFrom(10)
    println(tenOrMore) //Stream(10, ?)
    //    tenOrMore.map(i => i * i).take(5).foreach(println) //取前五个


    val stream = Stream(10) //Stream(10, ?)
    //    stream.map(i => i * i).foreach(println)


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
    indexes2("Mississippi").foreach(println(_))
  }


}