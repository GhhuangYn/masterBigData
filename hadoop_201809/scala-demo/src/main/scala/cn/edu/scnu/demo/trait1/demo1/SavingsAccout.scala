package cn.edu.scnu.demo.trait1.demo1

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/11 8:35
  */
abstract class SavingsAccount extends Account with Logger {

  def withdraw(amount:Double):Unit={
    if (amount>1000) log("ha ha h")
    else println("no no no")
  }
}
