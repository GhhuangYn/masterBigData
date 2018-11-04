package cn.edu.scnu.demo.trait1

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/10 23:32
  */
object Demo {

  def main(args: Array[String]): Unit = {
    val savingsAccount = new SavingsAccount
    savingsAccount.log("hahah")
    savingsAccount.withdraw(10000)
  }
}
