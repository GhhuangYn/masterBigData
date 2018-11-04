package mooc.log.util

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{LongType, StringType, StructField, StructType}

/**
  * @Description: 访问日志转换（输入=>输出工具类）
  *               输入：访问时间、访问URL、耗费的流量、访问IP地址信息
  *               输出：URL、cmsType(video/article)、cmsId(编号)、流量、ip、城市信息、访问时间、天
  * @author: HuangYn 
  * @date: 2018/10/26 20:05
  */
object AccessConvertUtil {

  //定义字段结构
  val struct = StructType(
    List(
      StructField("url", StringType, nullable = false),
      StructField("cmsType", StringType, nullable = false),
      StructField("cmsId", LongType, nullable = false),
      StructField("traffic", LongType, nullable = false),
      StructField("ip", StringType, nullable = false),
      StructField("city", StringType, nullable = false),
      StructField("time", StringType, nullable = false),
      StructField("day", StringType, nullable = false)
    )
  )

  def convert(log: String): Row = {

    val line = log.split("\t")
    val date = line(0)
    val url = line(1)
    val traffic = line(2).toLong
    val ip = line(3)

    val tmpURL = url.replace("http://", "")
    val cmsType = tmpURL.split("/")(1)
    val cmsId = tmpURL.split("/")(2).toLong
    val city = IPUtils.getCity(ip)

    Row(url, cmsType, cmsId, traffic, ip, city, date, date.split(" ")(0).replace("-", ""))
  }

}
