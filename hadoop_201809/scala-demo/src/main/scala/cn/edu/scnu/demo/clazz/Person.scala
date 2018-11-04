package cn.edu.scnu.demo.clazz

import java.time.LocalDateTime

import scala.beans.BeanProperty
import scala.collection.mutable.ArrayBuffer

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/10 8:35
  */
class Person(var id: Int = 0, val country: String = "") {

  //在主构造器中使用参数param:Type 这样的参数时，如果没有方法调用param，则此参数不会升格为字段

  /*  如果没有显式指定一个主构造器，那么会自动拥有一个无参的主构造器
      主构造器创建的方式
      1.放在类名后面,初始化时直接传入参数new Person(name,age)
      2.主构造器会执行类中定义的所有语句
      3.通常可以在主构造器中使用默认参数
   */

  //这是属于主构造器的语句，每次调用主构造器都会调用此句
  println("Just constructed a new Person")

  //辅助构造器，类似于java的构造器
  def this(name: String, age: Int) {
    this() //调用主构造器
    println("Person Assistant Constructor")
    this.id = Person.lastNumber //调用派生对象的方法
    this.name = name
    this.privateAge = age
  }

  //辅助构造器
  def this(name: String) {
    this()
    this.name = name
  }

  private var privateAge = 0

  /*
    @BeanProperty
    按照javaBean规范生成4个方法
     1.name:String
     2.name_=(newValue:String):Unit={}
     3.getName():String
     4.setName(newValue:String):Unit={}
   */
  @BeanProperty var name: String = _  //占位符会先给变量赋一个默认值

  //只读属性，只有getter
  val time = LocalDateTime.now()

  //其他对象无法访问这个字段,可以在[]指定可以访问的类名
  private[this] var addr = "gz"

  /*
    scala对每个字段都会自动生成getter和setter方法，其中getter叫age，setter叫age_=
    如果是私有字段，那么getter和setter也是私有的
    如果字段是val，那么只有getter方法
    如果不需要任何getter和setter，那么可以将字段声明为private[this]
   */

  //重写getter和setter
  def age: Int = privateAge

  def age_=(newAge: Int): Unit = {
    println("Person age_")
    if (newAge > privateAge) {
      privateAge = newAge
    }
  }

  def sayHi(): Unit = {
    println(s"hi $name")
  }

  //重写父类方法，必须使用override声明
  override def toString: String = {
    s"${getClass.getName}[name=$name,id=$id,age=$age]"
  }

  //定义内部类
  class Member(val name: String) {
    val contacts = new ArrayBuffer

  }

}

/*
    在scala中没有静态方法和静态字段，所以在scala中可以用object来实现这些功能，
    直接用对象名调用的方法都是采用这种实现方式

    伴生对象：通常会用到既有实例方法又有静态方法的类
    这时候可以通过伴生对象来达到目的
    伴生对象和类可以相互访问私有特性，需要在同一个源文件中
 */
object Person {
  private var lastNumber = 0

  private def newUniqueNumber(): Int = {
    lastNumber += 1
    lastNumber
  }

  //apply方法
  def apply(id: Int): Person = {
    new Person("lucy")
  }
}


