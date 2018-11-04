package cn.edu.scnu.demo

import java.io.File
import java.util
import java.util.Scanner

import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.{SortedMap, mutable}

/**
  * @Description: 映射和元组
  * @author: HuangYn 
  * @date: 2018/10/9 19:54
  */
object MapDemo {

  def main(args: Array[String]): Unit = {

    //===========构造映射===============

    /*
        Scala中，映射是对偶的集合
     */

    //不可变的映射
    //    val scores = Map("Alice" -> 10, "Leo" -> 20, "Cindy" -> 30)    //[String,Int]
    //也可以这样定义对偶
    //    Map(("Alice", 10), ("Leo" ,20), ("Cindy" ,30))

    //可变的映射
    val scores = scala.collection.mutable.Map("Alice" -> 10, "Leo" -> 20, "Cindy" -> 30)

    //空映射
    val nullMap = scala.collection.mutable.Map[String, Int]()

    //取值
    println(scores.contains("Leo")) //true
    val leoScore = if (scores.contains("Leo")) scores("Leo") else 0
    println("leoScore " + leoScore)
    println("getOrElse " + scores.getOrElse("Leo", 0))

    //get: 返回一个option对象,要么是Some(键的对应值) 要么是None
    val option = scores.get("Leo")
    println("option: " + option) //Some(20)

    //==============更新值 针对可变Map==============
    scores("Leo") = 21
    scores("Lulu") = 31
    scores += ("lily" -> 12, "Gigi" -> 13)
    scores.-=("Lulu") //移除

    //==============迭代==============
    //    for ((k, v) <- scores) {
    //      println(k + " -> " + v)
    //    }

    //    for (v<-scores.values){
    //      println(v)
    //    }

    //==============排序==============
    //SortedMap 按照key顺序
    val sortedScores = SortedMap("Alice" -> 10, "Leo" -> 20, "Cindy" -> 30)
    //    println(sortedScores.mkString("[",",","]"))

    //LinkedHashMap 按照输入顺序
    val sortedScores2 = mutable.LinkedHashMap("Alice" -> 10, "Leo" -> 20, "Cindy" -> 30)
    //    println(sortedScores2.mkString("[",",","]"))


    //=============元组(tuple) 不同类型的值的集合===============
    val t = (1, 3.14, "leo")
    val second1 = t._2
    println("second1: " + second1)
    val (first, second, third) = t //匹配模式
    println("first: " + first)

    //元组可以用于函数返回不止一个值的情况
    val (low, upper) = "New York".partition(_.isUpper)
    println(s"low: $low" + s"  upper: $upper") //low: NY  upper: ew ork

    //==================拉链操作 zip 把不同类型的数组连接成一个数组========================
    val symbols = Array("<", "-", ">")
    val counts = Array(2, 10, 3)
    val pairs = symbols.zip(counts)
    println(pairs.getClass.getSimpleName) //查看类名，可以看出是一个Tuple

    //Tuple2(("<",2),("-",10),(">",3))

    //处理k-v
    //    for ((k, v) <- pairs)
    //      println(s"k=$k,v=$v")

    //转换成映射
    //    val pairMap = pairs.toMap
    //    for ((k, v) <- pairMap) {
    //      println(s"$k:$v")
    //    }

    //练习1
    val products = Map("Notebook" -> 1000, "Phone" -> 2000, "Mouse" -> 90)
    val newProducts = products.mapValues(v => v * 0.9)
    //    for ((k, v) <- newProducts){
    //      println(s"$k:$v")
    //    }

    //练习2
    //    val in = new Scanner(new File("G:\\words.txt"))
    //    val words = in.nextLine().split(" ")
    //    val wordCount = scala.collection.mutable.SortedMap[String, Int]()
    //    words.foreach(w => {
    //      if (wordCount.get(w).eq(None)) {
    //        wordCount.put(w, 1)
    //      } else {
    //        val c = wordCount(w)
    //        wordCount(w) = c + 1
    //      }
    //    })
    //    for ((w, c) <- wordCount) {
    //      println(s"$w: $c")
    //    }

    //练习3
    //    val environment = System.getProperties.toMap
    //    for ((k, v) <- environment) {
    //      println(s"$k\t|$v")
    //    }

    //练习4 编写minmax函数，返回元组
    //    val tt = minmax(Array(1, 2, 3, 4, 5))
    //    println("min: "+tt._1)
    //    println("max: "+tt._2)

    //练习5
    val tt = lteqgt(Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 5, 6, 5), 5)
    println(tt)

    //练习6
    val zips = "hello".zip("world")
    println(zips)   //Vector((h,w), (e,o), (l,r), (l,l), (o,d))
  }

  def minmax(values: Array[Int]): (Int, Int) = {
    val min = values.min
    val max = values.max
    (min, max)
  }

  /**
    * 返回数组中小于v，大于v，等于v的数量，要求三个值一起返回
    *
    * @param values
    * @param v
    */
  def lteqgt(values: Array[Int], v: Int) = {
    var lt = 0
    var gt = 0
    var eq = 0
    values.foreach(x => {
      if (x > v) {
        gt += 1
      } else if (x == v) {
        eq += 1
      } else {
        lt += 1
      }
    })
    (lt, eq, gt)
  }


}
