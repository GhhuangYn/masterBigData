package demo.task

import java.sql.{Connection, DriverManager, PreparedStatement}

/**
  * @Description:
  * @author: HuangYn 
  * @date: 2018/10/30 9:15
  */
object MySqlUtil {

  def getConn(): Connection = {
    Class.forName("com.mysql.jdbc.Driver")
    val conn = DriverManager.getConnection(
      "jdbc:mysql://node00:3306/imooc?characterEncoding=UTF8&useSSL=false", "root", "123456")
    conn
  }

  def truncate(conn: Connection, table: String): Unit = {
    val pst = conn.prepareStatement("truncate "+ table)
    pst.executeUpdate()
  }

  def release(conn: Connection, pst: PreparedStatement): Unit = {
    try {
      if (pst != null) {
        pst.close()
      }
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      if (conn != null)
        conn.close()
    }
  }

}
