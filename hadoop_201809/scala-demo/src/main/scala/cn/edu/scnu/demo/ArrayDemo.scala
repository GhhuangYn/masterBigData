package cn.edu.scnu.demo

import java.util.TimeZone

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object ArrayDemo {

  def main(args: Array[String]): Unit = {

    //=====定长数组=====
    //定义数组
    val nums = new Array[Int](10)
    //    nums.foreach(n => println(n))
    nums(0) = 20
    //    println(nums(0))

    //给定初始值
    val s = Array("s1", "s2")
    //    println(s(0))

    //======变长数组=======
    val b = new ArrayBuffer[Int]()
    b += 1 //在末尾添加元素
    b += 23 //在末尾添加元素
    b += (1, 2, 3, 4)
    b ++= Array(5, 6) //追加集合 ++=
    b.trimEnd(2) //移除后两个元素
    b.insert(0, 100, 101)
    //    b.remove(3)
    //    b.foreach(b=>println(b))

    val c = b.toArray //数组缓冲转换为定长数组

    //========遍历数组==========
    //    c.foreach(c => println(c))

    //    for (i <- 0 until c.length) {
    //      println(s"index-$i:${c(i)}")
    //    }

    //    for (i <- c.indices) {    //遍历坐标
    //        println(s"$i: ${c(i)}")
    //    }

    //    for (e <- c) {
    //      println(s"$e")
    //    }

    //===========转换数组==============

    //从原始数组出发，以某种方式对它进行转换
    val a = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    //下面的做法完全是相同的，只是风格不同，一个用yield，一个使用函数式编程
    val newA = for (elem <- a if elem % 2 != 0) yield elem * 2 //过滤偶数
    //    val newAA = a.filter(elem => elem % 2 != 0).map (elem => elem * 2)
    val newAA = a.filter(_ % 2 != 0).map(_ * 2) //占位符可以省略

    //    newA.foreach(newA => print(newA + " "))
    //    println()
    //    newAA.foreach(a => print(a + " "))

    //===========常用算法==============
    //    println(a.sum)
    //    println(a.max)
    val sortedA = a.sortWith(_ > _)
    //    sortedA.foreach(e => print(e + " "))

    println("elem > 5 :" + a.count(_ > 6)) //有多少个大于5的元素

    //缓冲数组不能排序

    //以字符串的方式显示数组
    val stringA = a.mkString("<", " ", ">")
    println(stringA)

    //===========多维数组===============
    val matrix = Array.ofDim[Double](3, 4) //3行4列
    matrix(2)(3) = 4.9
    println(matrix(2)(3))

    //练习1
    //    val n = 10
    //    val randomArr = new Array[Int](n)
    //    for (i <- randomArr.indices) {
    //      randomArr(i) = Random.nextInt(n)
    //    }
    //    println(randomArr.mkString(" "))

    //练习2
    var tmp = 0
    val arr = Array(1, 2, 3, 4, 5)
    for (i <- arr.indices by 2) {
      if (i < arr.length - 1) {
        tmp = arr(i + 1)
        arr(i + 1) = arr(i)
        arr(i) = tmp
      }
    }
    println("swap: " + arr.mkString(" "))

    //练习3  反转数组
    println("reverse: " + arr.reverse.mkString(" "))

    //练习4 去掉重复元素
    val arr2 = Array(1, 1, 2, 3, 4, 5, 5, 4)
    val newArr2 = arr2.distinct
    println("distinct: " + newArr2.mkString(" "))

    //练习5 去掉缓冲数组除第一个负数外的所有负数
    //    val arr3 = ArrayBuffer(1, -3, -4, 5, 6, 7, -6, 7, 8, -4)
    //    val negative = new ArrayBuffer[Int]
    //    for (i <- arr3.indices) {
    //      if (arr3(i) < 0) {
    //        negative += i
    //      }
    //    }
    //    negative.remove(0)
    //    for (e <- negative.reverse) { //需要反转序列，否则arr3的标一直变化
    //      arr3.remove(e)
    //    }
    //    println(arr3.mkString(" "))


    //练习6
    val timezones = TimeZone.getAvailableIDs()
      .filter(_.contains("America"))
      .map(_.drop("America/".length))

    println(timezones.mkString("\n"))

  }


}
