package cn.edu.scnu.demo

import java.sql.DriverManager
import scala.xml

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/16 11:50
  */
object DataDemo {

  def main(args: Array[String]): Unit = {
//    testMysqlGet()
//    testGetUrl()
    testXML()
  }

  def testMysqlGet(): Unit = {
    val url = "jdbc:mysql://localhost:3306/spring"
    classOf[com.mysql.jdbc.Driver]
    val conn = DriverManager.getConnection(url, "root", "90741xx")
    val prep = conn.prepareStatement("select * from blog")
    prep.execute()
    val result = prep.getResultSet
    while (result.next()) {
      println(result.getString("content"))
    }
  }

  def testGetUrl(): Unit = {
    val content = scala.io.Source.fromURL("http://storm.apache.org/releases/1.2.2/storm-kafka-client.html")
    println(content.mkString)
  }

  //使用scala的xml需要引入scala-xml模块
  def testXML(): Unit = {
//    val items = <li>Fred</li><li>Wilma</li>   //items的类型 scala.xml.NodeBuffer
    val doc = <html href="www.baidu.com"><head><title>Tred's Memoris</title></head></html>    //scala.xml.Elem
    println(doc.label)
    println(doc.child)   //NodeBuffer(<head><title>Tred's Memoris</title></head>)
    println(doc.attribute("href"))
  }
}
