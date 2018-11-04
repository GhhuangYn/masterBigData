import java.io.{PrintWriter, _}
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date

import com.google.common.io.Files

import scala.io.Source

object Simulator {
  def main(args: Array[String]) {
    var i = 0
    while (true) {
      val filename = "G:\\test.log"
      val lines = Source.fromFile(filename).getLines.toList
      val filerow = lines.length
      val writer = new PrintWriter(new File("G:\\data\\streaming\\streamingdata" + i + ".txt"))
      i = i + 1
      var j = 0
      while (j < 100) {
//        Files.append(lines(index(filerow)) + "\n", new File("G:\\data\\streaming\\streamingdata" + i + ".txt"), Charset.defaultCharset())
        writer.write(lines(index(filerow)) + "\n")
        println(lines(index(filerow)))
        j = j + 1
      }
      writer.close()
      Thread sleep 5000
      log(getNowTime(), "G:\\streaming\\streamingdata" + i + ".txt generated")
    }
  }

  def log(date: String, message: String) = {
    println(date + "----" + message)
  }

  def index(length: Int) = {
    import java.util.Random
    val rdm = new Random
    rdm.nextInt(length)
  }

  def getNowTime(): String = {
    val now: Date = new Date()
    val datetimeFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    val ntime = datetimeFormat.format(now)
    ntime
  }
}