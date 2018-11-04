package cn.edu.scnu.demo.implict

import java.io.File
import scala.io.Source._

/**
  * @Description:  隐式转换就是：当Scala编译器进行类型匹配时，
  *              如果找不到合适的候选，那么隐式转化提供了另外一种途径来告诉编译器如何将当前的类型转换成预期类型。
  * @author: HuangYn 
  * @date: 2018/10/16 13:39
  */
//原来的File类型转换成一个新的RichFile类型
class RichFile(val from: File) extends AnyVal {
  def read = fromFile(from).mkString
}

object ImplictDemo {

  //隐式函数要放在调用之前
  implicit def file2RichFile(from: File) = new RichFile(from)

  def main(args: Array[String]): Unit = {
    //可以在File对象调用read方法它被隐式地转换成一个RichFile
    val contents = new File("G:\\hello.txt").read
    println(contents)
  }

}


