package cn.edu.scnu.demo.func

object FuncDemo {

  def main(args: Array[String]): Unit = {
//    println(fac(4))
//    println(fac2(4))
//    println(decorate("hello", "<<"))
//    println(sum(1, 2, 3, 4))
//    println(sum(1, 2, 3, 4, 5))
//    println(sum(1 to 5: _*))
//    box("hello")

//    print({}) //空块的类型是()


    //懒值
    //    lazy val words = scala.io.Source.fromFile("G:\\hah.txt").mkString
    //只有在调用words时，才会真正去访问该文件
    //    println(words)

    println(speed(time=10,distance = 100))

  }


  //不使用return，不指定返回类型，scala会自动检测
  def fac(n: Int) = {
    var r = 1
    for (i <- 1 to n) {
      r = r * i
    }
    r
  }

  //如果是递归函数，必须指定返回类型
  def fac2(n: Int): Int = {
    if (n > 1) n * fac2(n - 1)
    else 1
  }

  //默认参数
  def decorate(str: String, left: String = "[", right: String = "]") = {
    left + str + right
  }

  //变长参数
  def sum(args: Int*) = {
    var result = 0
    for (arg <- args) result += arg
    result
  }

  //没有返回值的函数，也叫做过程，可以设定返回类型为Unit，相当于void
  def box(s: String): Unit = {
    println(s)
  }

  //命名参数，在函数调用时可以不按顺序传参，不过需要指定参数名
  def speed(distance: Double, time: Double): Double = {
    distance / time
  }
}
