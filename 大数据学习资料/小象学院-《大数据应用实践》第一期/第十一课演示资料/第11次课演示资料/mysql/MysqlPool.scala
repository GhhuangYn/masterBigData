package bigadta.spark.mysql

/**
 * Created by xiaoguanyu on 2018/1/23.
 */
import java.sql.Connection
import com.mchange.v2.c3p0.ComboPooledDataSource

class MysqlPool extends Serializable {
  private val cpds: ComboPooledDataSource = new ComboPooledDataSource(true)
  try {
    cpds.setJdbcUrl("jdbc:mysql://192.168.183.101:3306/streamdb");
    cpds.setDriverClass("com.mysql.jdbc.Driver");
    cpds.setUser("hive");
    cpds.setPassword("hive123")
    cpds.setMaxPoolSize(200)
    cpds.setMinPoolSize(20)
    cpds.setAcquireIncrement(5)
    cpds.setMaxStatements(180)
  } catch {
    case e: Exception => e.printStackTrace()
  }
  def getConnection: Connection = {
    try {
      return cpds.getConnection();
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        null
    }
  }
}
object MysqlManager {
  var mysqlManager: MysqlPool = _
  def getMysqlManager: MysqlPool = {
    synchronized {
      if (mysqlManager == null) {
        mysqlManager = new MysqlPool
      }
    }
    mysqlManager
  }
}