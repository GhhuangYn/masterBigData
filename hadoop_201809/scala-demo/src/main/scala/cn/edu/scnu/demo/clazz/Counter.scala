package cn.edu.scnu.demo.clazz

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/10 8:20
  */
class Counter {

  private var value = 0

  def increment() {
    value += 1
  }

  def current() = value


}
