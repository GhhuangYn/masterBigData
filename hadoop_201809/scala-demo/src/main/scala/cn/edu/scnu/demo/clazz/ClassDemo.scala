package cn.edu.scnu.demo.clazz

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/10 8:32
  */
object ClassDemo {

  def main(args: Array[String]): Unit = {
    val counter = new Counter
    counter.increment()
    println(counter.current())

    val person = new Person
    person.age = 12 //调用age_=方法，相当于setter
    println(person.age) //调用Person类的age方法，相当于getter
    person.setName("leo")
    println(person.name + " " + person.id)

    val p2 = new Person("lulu", 10)
    //查看Person声明的所有字段
    println("====fields====")
    //        p2.getClass.getDeclaredFields.foreach(f => println(f))
    println("====methods====")
    //        p2.getClass.getDeclaredMethods.foreach(m => println(m))

    //内部类调用
    val members = new p2.Member("CC")

    //测试Time
    val t1 = new Time(10, 22)
    val t2 = new Time(10, 28)
    //    println("t1 before t2 : " + t1.before(t2))

    //使用apply方法构建对象
    val p3 = Person(3) //如果使用new，使用的是构造器进行构造
    p3.name = "bobby"
    p3.id = 10
    println(s"p3 $p3")
    println(p3.getClass.getSuperclass) //class java.lang.Object


    val e1 = new Employee("dabian", 4, 199)
    val e2 = new Employee("dabian",4, 200)
    e1.id = 2
    println(s"e1 $e1")
    println(s"e1 == e2 : ${e1 == e2}")
    //    e1.getClass.getDeclaredFields.foreach(f => println(f))
    //    e1.getClass.getDeclaredMethods.foreach(f => println(f))

    println(e1.getClass.getSuperclass)

    //类型检查
    //    if (e1.isInstanceOf[Person]) {
    //      val p4 = e1.asInstanceOf[Person] //转化为子类引用
    //      p4.sayHi()
    //    }


  }
}
