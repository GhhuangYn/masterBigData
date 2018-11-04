package cn.edu.scnu.demo.file

import java.io.PrintWriter
import java.nio.file._

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/10 16:47
  */
object FileDemo {

  def main(args: Array[String]): Unit = {
    //    val source = Source.fromFile("G:\\words.txt", "UTF8")
    //source只能使用一次，下次再用就读取不了数据
    //    source.getLines().foreach(line => println(line))
    //    val content = source.mkString
    //    println(content)
    //    val words = content.split("\\s+")
    //    words.filter(_.length > 7).foreach(w => println(w))
    //    source.close()

    //写入文本:scala没有内建对写入文本的支持，需要借用java
    //    val out = new PrintWriter("G:\\number.txt")
    //    for (i <- 1 to 100)
    //      out.print(i + "\t")
    //    out.close()

    //访问目录
    //    val dir = "G:/MyDrivers"
    //    Files.walk(Paths.get(dir)).forEach(p => println(p))


    //练习1 反转文本的行数 使倒序
    //    val source = Source.fromFile("G:\\words.txt")
    //    val lines = source.getLines().toArray.reverse
    //    val out = new PrintWriter("G:\\reverse.txt")
    //    for (line <- lines) {
    //      out.write(line + "\r\n")
    //    }
    //    out.close()
    //    source.close()

    //练习2 读取文本中的浮点数，计算平均值、和、最大值、最小值
    val source = Source.fromFile("G:\\words.txt")
    val ints = new ArrayBuffer[Int]
    source.getLines().foreach(line => {
      val words = line.split(" ")
      ints ++= words.filter(_.matches("[0-9]+")).map(_.toInt).toArray
    })
    var sum = 0
    var max = 0
    var min = Int.MaxValue
    ints.foreach(u => {
      sum += u
      if (u > max)
        max = u
      if (u < min)
        min = u
    })
    println(s"sum=$sum,avg=${sum / ints.size},max=$max,min=$min")


    //练习3
    val out = new PrintWriter("G:\\result.txt")
    var m = 0d
    for (n <- 0 to 20) {
      m = Math.pow(2, n)
      out.write(Math.round(m) + "\t\t" + Math.round((1.0 / m)*1000000)/1000000.0 + "\r\n")
    }
    out.close()

  }
}
