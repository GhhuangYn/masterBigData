package bigadta.spark.kafka
import java.util.Properties
import org.apache.kafka.clients.producer.{ProducerRecord, KafkaProducer}
import scala.util.Random

/**
 * kafka Producer
 */
object KafkaEventProducer {

  //不同设备客户端版本号
  private val devices = Array(
    "apple_phone_v1.2_20180101",
    "android_v1.2_20180110",
    "windows_v1_20171201",
    "apple_pad_v1.2_20180101",
    "apple_mac_v1.2_20180101",
    "android_pad_v1.2_20180110")

  //不同设备客户端对应的设备类型
  private val deviceTypes = Map(
    "apple_phone_v1.2_20180101" -> "mobile",
    "android_v1.2_20180110" -> "mobile",
    "windows_v1_20171201" -> "pc",
    "apple_pad_v1.2_20180101" -> "mobile",
    "apple_mac_v1.2_20180101" -> "pc",
    "android_pad_v1.2_20180110" -> "mobile"
  )

  private val random = new Random()

  private var pointer = -1
  //获取设备客户端版本号和设备类型
  def getDeviceInfo() : String = {
    pointer = pointer + 1
    if(pointer >= devices.length) {
      pointer = 0
      devices(pointer) + "|" + deviceTypes(devices(pointer))
    } else {
      devices(pointer) + "|" + deviceTypes(devices(pointer))
    }
  }
  //模拟点击值
  def click() : Int = {
    random.nextInt(100)
  }

  def main(args: Array[String]): Unit = {
    val props: Properties = new Properties
    props.put("bootstrap.servers", "192.168.183.102:9092,192.168.183.103:9092,192.168.183.104:9092")
    props.put("acks", "1")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props);
    while(true) {
      val eventTime = System.currentTimeMillis.toString
      val event = getDeviceInfo() + "|" + eventTime + "|" + click()
      producer.send(new ProducerRecord[String, String]("DeviceEvents",event.toString ))
      println("Message sent: " + event)
      Thread.sleep(500)
    }
    producer.close()
  }
}
