package cn.edu.scnu.demo.obj

/**
  * @Description: object定义了某个类的单个实例
  * @author: HuangYn 
  * @date: 2018/10/10 10:36
  */
object Account {

  private var lastNumber = 0

  def newUniqueNumber: Int = {
    lastNumber += 1
    lastNumber
  }

  def main(args: Array[String]): Unit = {
    println(Account.newUniqueNumber)
  }
}
