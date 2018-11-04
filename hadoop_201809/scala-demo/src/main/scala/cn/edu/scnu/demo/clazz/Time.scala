package cn.edu.scnu.demo.clazz

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/10 10:09
  */
class Time(val hours: Int, val minutes: Int) {

  def before(other: Time): Boolean = {
    if (this.hours <= other.hours) {
      if (this.hours < other.hours) true
      else if (this.minutes < other.minutes) true
      else false
    }
    else false
  }

}
