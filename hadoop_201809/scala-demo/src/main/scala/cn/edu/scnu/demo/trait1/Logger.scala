package cn.edu.scnu.demo.trait1

/**
  * @Description: 特质
  * @author: HuangYn 
  * @date: 2018/10/10 22:37
  */
trait Logger {

  //这是个抽象方法，无需使用abstract
  def log(msg: String)

  //可以带有实现方法
  def myName(): String = {
    getClass.getSimpleName
  }

}
