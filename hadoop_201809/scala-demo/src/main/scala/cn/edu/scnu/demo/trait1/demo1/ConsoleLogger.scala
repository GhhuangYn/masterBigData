package cn.edu.scnu.demo.trait1.demo1

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/11 8:34
  */
trait ConsoleLogger extends Logger{

  def log(msg:String){println(msg)}
}
