package mooc.log.util

import java.sql.{Connection, DriverManager, PreparedStatement}

/**
  * @Description: 数据库操作工具
  * @author: HuangYn 
  * @date: 2018/10/27 9:23
  */
object MysqlUtil {

  def Connection(): Connection = {
    DriverManager.getConnection("jdbc:mysql://node00:3306/imooc?characterEncoding=utf8", "root", "123456")
  }

  def release(connection: Connection, preparedStatement: PreparedStatement): Unit = {

    try {

      if (preparedStatement != null)
        connection.close()
    } catch {
      case e: Exception => e.printStackTrace()
      case _ => println("未知错误!")
    } finally {
      if (connection != null)
        connection.close()
    }

  }
}
