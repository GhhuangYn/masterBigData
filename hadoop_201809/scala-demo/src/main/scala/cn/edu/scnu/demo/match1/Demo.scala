package cn.edu.scnu.demo.match1

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/12 12:43
  */
object Demo {

  def main(args: Array[String]): Unit = {

    //更好的switch
    val ch = '1'
    val sign = ch match {
      case '+' => 1
      case '-' => -1
      case _ => 0
    }
    println(sign)

    //守卫(条件判断)
    ch match {
      case _ if Character.isDigit(ch) => println("digit")
      case '+' => println("plus")
      case '-' => println("minus")
      case _ => println("none")
    }

    //模式中的变量
    var arr = Array(1, 2, 3, 4)
    val chch = 4
    arr(3) match {
      case '+' => println("plus")
      case '-' => println("minus")
      case `chch` => println(chch) //变量名要加 ``
    }

    //类型模式
    def matchType(obj: Any) = {
      obj match {
        case x: Int => println("Int")
        case s: String => println("String"); Integer.parseInt(s)
        case m: Map[String, Int] => m.foreach(println(_))
        case _ => println("unknown")
      }
    }

    matchType("4")

    //匹配数组
    arr = Array(0, 1, 2)
    arr match {
      case Array(0, 1) => println(0) //匹配有且只有一个0的数组
      case Array(x, y) => println(s"$x $y") //匹配任何带有两个元素的数组
      case Array(0, rest@_*) => println(rest) //匹配以0开始的任何数组,并把把参数绑定到变量
      case _ => println("_")
    }

    //匹配列表
    val lst = List(0, 1, 2, 3)
    lst match {
      case 0 :: Nil => println("0")
      case x :: y :: Nil => println(s"$x,$y,Nil")
      case 0 :: tail => println("0 ...") //0开头
      case _ => println("something else")
    }

    //匹配元组
    val pair = (0, 10)
    pair match {
      case (0, _) => println("0 _")
      case (y, 0) => println(s"$y 0")
      case _ => println("neither is 0")
    }

    //option的类型匹配
    val map = collection.immutable.Map[String, Int]("leo" -> 90, "vivid" -> 87)
    val result = map.get("ee")
    result match {
      case Some(score) => println(score)
      case None => println("no score")
    }
    //不过上面的类型匹配很繁琐，一般使用option的getOrElse()
    println(result.getOrElse("no score"))   //如果没有值，指定默认值

  }

}
