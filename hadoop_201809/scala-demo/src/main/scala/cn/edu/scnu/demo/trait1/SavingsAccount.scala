package cn.edu.scnu.demo.trait1

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/10 22:46
  */
class SavingsAccount extends Account with Logger {
  override def log(msg: String): Unit = {
    println(msg)
  }
  def withdraw(amout:Double):Unit={
    if (amout>1000) println(myName())
    else log("哈哈哈")
  }
}
