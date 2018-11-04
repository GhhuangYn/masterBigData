package cn.edu.scnu.demo.clazz

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/10 11:40
  */
class Employee(name: String, age: Int, salary: Double) extends Person(name, age) { //name age传递到超类

  //自类主构造器中的参数必须加var 才可以直接访问到

  println("this is Employee Constructor")

  /*
      在子类的字段中  重写字段使用override修饰 编译器自动加上getter/setter方法
      def只能重写另一个def
      val只能重写另一个val或不带参数的def
      var只能重写另一个抽象的var
   */
  override val country = "CN"

  override def sayHi(): Unit = {
    println(s"hello $name")
  }

  override def toString: String = {
    s"${getClass.getName}[name=$name,age=$age,salary=$salary]"
  }

  override def hashCode(): Int = {
    name.hashCode - age.hashCode
  }

  override def equals(obj: scala.Any): Boolean = {
    val o = obj.asInstanceOf[Employee]
    o.hashCode() == this.hashCode()
  }
}
