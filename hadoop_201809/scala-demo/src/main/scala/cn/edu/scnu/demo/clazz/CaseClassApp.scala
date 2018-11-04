package cn.edu.scnu.demo.clazz

/**
  * @Description:  case class通常用在模式匹配
  * @author: HuangYn 
  * @date: 2018/10/15 22:18
  */
object CaseClassApp {
  def main(args: Array[String]): Unit = {
    println(Dog("wang cai").name)
  }
}

case class Dog(name:String)
