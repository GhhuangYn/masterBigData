package cn.edu.scnu.demo.trait1.demo1

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/10 23:32
  */
object Demo {

  def main(args: Array[String]): Unit = {
    val savingsAccount = new SavingsAccount with ConsoleLogger //对于抽象类，可以再创建新对象时混入一个具体的日志记录器实现
    savingsAccount.log("hahah")
    savingsAccount.withdraw(10000)
  }
}
