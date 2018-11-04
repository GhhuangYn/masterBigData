package cn.edu.scnu.demo.func

import scala.collection.mutable
import scala.math._

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/11 11:34
  */
object AdvanceDemo {

  def main(args: Array[String]): Unit = {

    val num = 3.14
    val fun = ceil _ //_将ceil方法变成函数
    //        Array(3.14, 1.43, 2.0).map(fun).foreach(println(_))

    //匿名函数
    val triple = (x: Double) => x * 3
    //    Array(3.14, 1.43, 2.0).map(triple).foreach(println(_))

    //函数参数可以放在中括号内，而且可以用中置法表示(没有句点)
    //    Array(3.14, 1.43, 2.0) map { (x: Double) => x * 3 } foreach (println(_))
    //    Array(3.14, 1.43, 2.0) map triple foreach (println(_))

    //这是一个函数
    val f1 = (x: Double) => x * 3 //函数类型时 Double => Double

    //任何以def定义的都是方法不是函数
    def f2(x: Double) = 3 * x //方法类型是 (x:Double)Double

    //============高阶函数(high-order function): 带函数参数的函数=============
    def valueAtOneQuarter(f: Double => Double, n: Double) = f(n) //返回的是Double
    //    println(valueAtOneQuarter(ceil, 4.2))
    //    println(valueAtOneQuarter(sqrt, 4))
    //    println(valueAtOneQuarter(_ * 2, 4))

    //高阶函数可以产生另一个函数
    def mulBy(factor: Double) = (x: Double) => factor * x

    val quintuple = mulBy(5)
    //    println(quintuple(3))

    //有用的高阶函数
    //    (1 to 9).map("*" * _).foreach(println)

    //============柯里化：将原来接收两个参数的函数变成一个新的接收一个参数的函数的过程==============
    val mul = (x: Int, y: Int) => x * y //接收两个参数的函数
    val mulOneAtAtime = (x: Int) => (y: Int) => x * y //生成另外一个接收单个参数的函数
    //    println(mulOneAtAtime(4)(5))

    //简化版
    def mulOneAtATime2(x: Int)(y: Int) = x * y


    //偏函数:被包在花括号内的一组case语句是一个偏函数
    val f: PartialFunction[Char, Int] = {
      case '+' => 1
      case '-' => -1
    }
    f('-')
    //f('0')  //scala.MatchError: 0 (of class java.lang.Character)
    "-4+3+1".collect({ case '-' => -1; case '+' => 1 })   //匹配字符串中的每个字符

    //练习1
    def values(fun: (Int) => Int, low: Int, high: Int): mutable.SortedMap[Int, Int] = {
      val result = mutable.SortedMap[Int, Int]()
      (low to high).foreach(i => result(i) = fun(i))
      result
    }

    val result = values(x => x * x, -5, 5)
    //    for (elem <- result) {
    //      println(elem)
    //    }

    //练习2 使用reduceLeft返回最大值
    val max = Array(2, 3, 5, 1, 9, 10, 4).reduceLeft((x, y) => {
      //      println(s"$x,$y")
      if (x > y) x
      else y
    })
    println(s"max: $max")

    //练习3 使用reduceLeft实现阶乘
    //    println((1 to 4).reduceLeft(_ * _))

    //练习4 使用fold完成阶乘
    val n = 1
    val max2 = (0 to 4).foldLeft(n)((x, y) => {
      //      println(s"$x,$y")
      if (x >= n && y >= n)
        x * y
      else n
    })
    println(s"max2: $max2")

    //练习5 输出给定输入序列中函数的最大值
    def largest(f: (Int => Int), inputs: Seq[Int]): Int = {
      inputs.map(f(_)).max
    }

    val funcMax = largest(x => 10 * x - x * x, 1 to 10)
    println(s"funcMax: $funcMax")

    //练习6 返回上一题中最大值的输入
    def largest2(f: (Int => Int), inputs: Seq[Int]): Int = {
      inputs.maxBy(f(_))
    }

    val maxIndex = largest2(x => 10 * x - x * x, 1 to 10)
    println(s"maxIndex: $maxIndex")

    //练习7
    def adjustToPair(f: (Int, Int) => Int)(pairs: (Int, Int)): Int = {
      f(pairs._1, pairs._2)
    }

    def adjustToPair2(f: (Int, Int) => Int) = (paris: (Int, Int)) => {
      f(paris._1, paris._2)
    }

    var res = adjustToPair((x, y) => x * y)(3, 4)
    println(s"$res")
    val func = adjustToPair2((x, y) => x * y)
    res = func(6, 7)
    println(s"$res")

  }
}
