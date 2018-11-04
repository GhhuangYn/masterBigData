package mooc.udf

import org.apache.hadoop.hive.ql.optimizer.spark.SparkSkewJoinProcFactory.SparkSkewJoinJoinProcessor
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types._

/**
  * @Description: 自定义一个求平均数的聚合函数
  * @author: HuangYn 
  * @date: 2018/10/28 20:12
  */
object MyAverage extends UserDefinedAggregateFunction {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .master("local").appName("udf").getOrCreate()
    val json = spark.read.json("G:\\employees.json")
    //注册UDF函数
    spark.udf.register("myAverage", MyAverage)
    json.show()
    json.createOrReplaceTempView("employees")
    spark.sql("select myAverage(salary) from employees").show()
  }

  //输入数据的格式
  override def inputSchema: StructType = StructType(List(StructField("inputColumn", LongType, nullable = false)))

  //聚合缓冲区的数据类型
  override def bufferSchema: StructType = StructType(List(StructField("sum", LongType), StructField("count", LongType)))

  //返回值数据类型
  override def dataType: DataType = DoubleType

  //如果有相同的输入，函数是否总是有相同的输出
  override def deterministic: Boolean = true

  //初始化聚合缓冲区
  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer(0) = 0L //存放输入的值
    buffer(1) = 0L //存放输入的行数
  }

  //用输入数据更新buffer
  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    if (!input.isNullAt(0)) {
      buffer(0) = buffer.getLong(0) + input.getLong(0) //累加
      buffer(1) = buffer.getLong(1) + 1L //每次数目加1
    }
  }

  //合并两个聚合buffers并存储updated buffer的值到buffer1
  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    buffer1(0) = buffer1.getLong(0) + buffer2.getLong(0)
    buffer1(1) = buffer1.getLong(1) + buffer2.getLong(1)
  }

  //计算最后的结果
  override def evaluate(buffer: Row): Any = {
    buffer.getLong(0).toDouble / buffer.getLong(1)
  }
}
