package cn.edu.scnu.demo.clazz

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/15 22:05
  */
object ApplyApp {
  def main(args: Array[String]): Unit = {

    //    for (i <- 1 to 10) {
    //      ApplyTest.incr
    //    }
    //    println(ApplyTest.count)    //说明伴生对象是一个单例对象

    val b = ApplyTest() //==> object.apply
    val c = new ApplyTest() //==> class.apply

    //类名()   调用的是object的apply
    //new 类名()   调用的是class的apply

  }
}

/**
  * 伴生类和伴生对象
  * 如果有一个class，还有一个object与它同名
  * 那么这个object叫做class的伴生对象,class是该对象的伴生类
  */
class ApplyTest {
  def apply() = {
    println("class ApplyTest apply")
    new ApplyTest
  }
}

object ApplyTest {
  var count: Int = 0

  def incr = {
    count = count + 1
  }

  //最佳实践：在Object的apply方法中new class
  def apply(): ApplyTest = {
    println("Object ApplyTest apply")
    new ApplyTest
  }
}
