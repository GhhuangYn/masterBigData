package cn.edu.scnu.demo

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/16 11:03
  */
object StringDemo {

  def main(args: Array[String]): Unit = {
    //多行字符串(按住shift，按三次双引号)
    val b =
      """
        |这是一个多行字符串
        |hello
        |scala
      """.stripMargin
    println(b)
  }
}
