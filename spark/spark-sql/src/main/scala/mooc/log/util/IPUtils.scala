package mooc.log.util

import java.io.FileOutputStream
import java.time.{LocalDate, LocalTime}

import com.ggstar.util.ip.IpHelper
import org.apache.commons.io.IOUtils
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Random

/**
  * @Description: 生成中国的IP网段数据工具
  * @author: HuangYn 
  * @date: 2018/10/26 18:54
  */
object IPUtils {

  def main(args: Array[String]): Unit = {

//    println(getCity("121.200.199.255"))
    generateLog()
  }

  //根据ip返回城市
  def getCity(ip: String): String = {
    IpHelper.findRegionByIp(ip)
  }

  def etlIP(): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("IpUtils")
    val sc = new SparkContext(conf)
    sc.textFile("G:\\CN-20181026.txt").map(line => {
      val split_data = line.split("\t")
      split_data(0) + "\t" + split_data(1)

    }).saveAsTextFile("G:\\CN-IP")
  }

  def generateLog(): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("IpUtils")
    val sc = new SparkContext(conf)
    val ipArray = sc.textFile("G:\\CN-IP").flatMap(_.split("\t")).collect()
    val len = ipArray.length

    val host = "http://www.imooc.com/"
    val random = new Random()
    var ip = ""
    var url = ""
    var traffic = 0
    val types = Array("video", "article")
    var date = ""
    val fos = new FileOutputStream("G:\\access.log", true)

    for (i <- 1 to 1000000) {
      traffic = random.nextInt(500)
      url = host + types(random.nextInt(2)) + "/" + (random.nextInt(50) + 1)
      ip = ipArray(random.nextInt(len))
      date = LocalDate.of(2017, random.nextInt(2) + 6, random.nextInt(30) + 1) + " " + LocalTime.of(random.nextInt(24), random.nextInt(60), random.nextInt(60)).toString
      val data = date + "\t" + url + "\t" + traffic + "\t" + ip + "\r\n"
      IOUtils.write(data, fos, "UTF8")
    }
    fos.close()
  }
}
