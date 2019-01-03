/**
	1.Shuffle的定义 
	我们都知道Spark是一个基于内存的、分布式的、迭代计算框架。在执行Spark作业的时候，
	会将数据先加载到Spark内存中，内存不够就会存储在磁盘中，那么数据就会以Partition的方式存储在各个节点上，
	我们编写的代码就是操作节点上的Partiton数据。之前我们也分析了怎么我们的代码是怎么做操Partition上的数据，
	其实就是有Driver将Task发送到每个节点上的Executor上去执行，在操作Partiton上的数据时候，
	遇到Action操作的时候会生成一个新的Partition，而这个Partition是由多个节点上的Partition组成的，
	这样就实现了跨界点，我们管这种操作就叫Spark的Shuffle操作。其实比较比较通俗的来说，
	其实就是上一个Stage的输出，下一个Stage拉取这个输出的过程就是Shuffle。

*/

