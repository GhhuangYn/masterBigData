package cn.edu.scnu.demo.trait1

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/10 22:38
  */
class ConsoleLogger extends Logger with Serializable {  //继承多个特质用with关键字

  //重写特质抽象方法时无需使用override
  def log(msg: String): Unit = {
    println("consoleLogger...")
    println(msg)
  }
}
