package cn.edu.scnu.demo.implict

/**
  * @Description: 隐式转换函数的函数名可以是任意的，与函数名称无关，只与函数签名（函数参数和返回值类型）有关。
  * @author: HuangYn 
  * @date: 2018/10/16 15:39
  */


object ImplictDemo2 {

  implicit def int2float(x: Float): Int = x.toInt

  //string中没有bark方法，通过隐式转换，调用对用的方法转换。
  //隐式类的主构造函数参数有且仅有一个
  implicit class Dog(val name: String) {
    def bark = println(s"$name: bark bark bark ...")
  }

  //隐式参数
  def add(x: Int)(implicit z: String): Unit = {
    println(z.getClass.getTypeName) //double
    println(x + z)
  }

  def main(args: Array[String]): Unit = {
    val x: Int = 5.33f //编译器会隐式调用Float的int2float方法(本作用域中)
    println(x)

    "Leo".bark
    add(4)("")
  }


}


