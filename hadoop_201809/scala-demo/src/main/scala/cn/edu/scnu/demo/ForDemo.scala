package cn.edu.scnu.demo

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/9 11:00
  */
object ForDemo {

  def main(args: Array[String]): Unit = {
    val s = "hello"
    for (i <- 0 until s.length) {
      print(s(i))
    }

    //直接遍历字符串
    for (ch <- "hello") {
      print(ch)
    }
    println()

    //相当于两个for循环嵌套
    //    for (i <- 1 to 10; j <- 1 to 3) {
    //      println("i=" + i + ",j=" + j + ",i*j=" + i * j)
    //    }

    //添加条件
    //    for (i <- 1 to 10 if i != 3; j <- 1 to 3 if i != j) {
    //      println("i=" + i + ",j=" + j + ",i*j=" + i * j)
    //    }

    //    for (i <- 1 to 3; from = 4 - i; j <- from to 3) {
    //      println("i=" + i + ",j=" + j + ",i*j=" + i * j)
    //    }

    //yield会构建一个集合
    //    val col =
    //      for (i <- 1 to 4)
    //        yield i % 5
    //    println(col)

    //设置步长
    //    for (i <- 10 to (0,-1)) {
    //      print(i+" ")
    //    }

//    for (i <- 10 until 0 by -2) {
//      print(i + " ")
//    }

    //计算Hello每个字符的unicode乘积
    //    var mul = 1L
    //    for (ch <- "Hello") {
    //      mul *= ch.toInt
    //    }
    //    println(mul)

    //    var mul = 1L
    //    "Hello".iterator.foreach(c => mul *= c.toInt)
    //    println(mul)

    //字符串插值
    //    val name = "leo"
    //    val age = 12
    //    println(f"I am $name, ${age + 2} years old")
  }


}
