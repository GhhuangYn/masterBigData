package imooc.project

/**
  * 实战课程的点击数
  * @param day_course Hbase的rowkey
  * @param click_count 某天课程的点击数  20181111_124
  */
case class CourseClickCount(day_course: String,click_count:Long)
