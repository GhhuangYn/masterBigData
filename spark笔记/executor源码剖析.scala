
/*
	executor启动之后，executor会立刻向driver进行反向注册

	这里有一个重要的类，叫做CoarseGrainedExecutorBackend
	1.它在创建executor进程的时候会调用onStart()方法
	2.它是一个粗粒度进程
	3.onStart方法中，负责向Driver发送executor的注册信息
	4.它是一个通信进程，可以与Driver相互通信
	5.它负责接收Driver返回回来的executor的注册信息，然后创建Executor上下文
	6.它负责接收TaskSchedulerImp发送过来的LaunchTask消息，开始启动Tsk
*/

/*
	一、Executor执行Task的原理分析
	1.当CoarseGrainedExecutorBackend接收到Driver发送过来的RegisteredExecutor消息的时候就会创建Executor
	2.当再次接收Driver中的TaskScheduler发送LaunchTask消息时开始执行Task，首先它会对发送过来的
	  TaskDescription进行反序列化，然后交给executor的launchTask()方法
	3.在launchTask()方法中,创建了TaskRunner(继承了Runnable),然后加入到线程池中等待执行
*/

/* 二、Executor执行Task的源码分析 */

/***** CoarseGrainedExecutorBackend *****/

/*
	CoarseGrainedExecutorBackend的onStart方法：
	该方法在创建CoarseGrainedExecutorBackend类的时候被执行，它会向Driver注册Executor
*/
override def onStart() {
    logInfo("Connecting to driver: " + driverUrl)
    rpcEnv.asyncSetupEndpointRefByURI(driverUrl).flatMap { ref =>
      driver = Some(ref)
      // 向Driver发送注册请求
      ref.ask[Boolean](RegisterExecutor(executorId, self, hostname, cores, extractLogUrls))
    }(ThreadUtils.sameThread).onComplete {
      case Success(msg) =>
      case Failure(e) =>
        exitExecutor(1, s"Cannot register with driver: $driverUrl", e, notifyDriver = false)
    }(ThreadUtils.sameThread)
  }

// 在receive()方法，接收各种消息
override def receive: PartialFunction[Any, Unit] = {

	// 如果注册成功，Drvier会发送回来响应消息RegisteredExecutor
	case RegisteredExecutor =>
	  logInfo("Successfully registered with driver")
	  try {
	  	// 启动一个executor
	    executor = new Executor(executorId, hostname, env, userClassPath, isLocal = false)
	  } catch {
	    case NonFatal(e) =>
	      exitExecutor(1, "Unable to create executor due to " + e.getMessage, e)
	  }

	// TaskSchedulerImpl发送过来的要求启动Task的消息
	// executorData.executorEndpoint.send(LaunchTask(new SerializableBuffer(serializedTask)))
	// 这个消息的作用是要求executor执行Task任务
	case LaunchTask(data) =>
      if (executor == null) {
        exitExecutor(1, "Received LaunchTask command but executor was null")
      } else {
      	// 反序列化
        val taskDesc = TaskDescription.decode(data.value)
        logInfo("Got assigned task " + taskDesc.taskId)

        // 启动task
        executor.launchTask(this, taskDesc)
      }

}

/* 三、Executor中的launch()方法 */

// 启动task
def launchTask(context: ExecutorBackend, taskDescription: TaskDescription): Unit = {
	// 对于每一个task，都会创建一个taskRunner(继承了Runnable接口)线程
    val tr = new TaskRunner(context, taskDescription)
    // 放入缓存中
    runningTasks.put(taskDescription.taskId, tr)
    // 把线程放入线程池，等待执行
    threadPool.execute(tr)
}

//四、TaskRunner中的run()方法解析

override def run(): Unit = {
  threadId = Thread.currentThread.getId
  Thread.currentThread.setName(threadName)
  val threadMXBean = ManagementFactory.getThreadMXBean
  // 为task分配一个内存管理器
  val taskMemoryManager = new TaskMemoryManager(env.memoryManager, taskId)
  val deserializeStartTime = System.currentTimeMillis()
  val deserializeStartCpuTime = if (threadMXBean.isCurrentThreadCpuTimeSupported) {
    threadMXBean.getCurrentThreadCpuTime
  } else 0L
  Thread.currentThread.setContextClassLoader(replClassLoader)

  // 创建一个序列化器，用来对Task进行反序列化
  val ser = env.closureSerializer.newInstance()
  // 向Driver发送Task当前的执行状态
  execBackend.statusUpdate(taskId, TaskState.RUNNING, EMPTY_BYTE_BUFFER)
  var taskStart: Long = 0
  var taskStartCpu: Long = 0
  startGCTime = computeTotalGcTime()

  try {
    // Must be set before updateDependencies() is called, in case fetching dependencies
    // requires access to properties contained within (e.g. for access control).
    Executor.taskDeserializationProps.set(taskDescription.properties)
    // 通过网络通信，获取Task依赖的文件、资源、jar包，比如说Hadoop的配置文件
    updateDependencies(taskDescription.addedFiles, taskDescription.addedJars)
    // 通过反序列化将Task进行反序列化
    // 类加载的作用：用发射动态加载一个类，创建类的对象
    task = ser.deserialize[Task[Any]](
      taskDescription.serializedTask, Thread.currentThread.getContextClassLoader)
    task.localProperties = taskDescription.properties
    task.setTaskMemoryManager(taskMemoryManager)

    //如果在序列化之前以及被停掉了，那么就会马上退出，否则就会继续执行Task
    val killReason = reasonIfKilled
    if (killReason.isDefined) {
      throw new TaskKilledException(killReason.get)
    }

    env.mapOutputTracker.updateEpoch(task.epoch)

    // 运行实际的任务并且计算时间
    taskStart = System.currentTimeMillis()
    taskStartCpu = if (threadMXBean.isCurrentThreadCpuTimeSupported) {
      threadMXBean.getCurrentThreadCpuTime
    } else 0L
    var threwException = true

    // 这里的value就是MapStatus,执行Task所得结果其实就是shuffle操作，
    // shuffle操作后的结果会被持久化到对应的shuffle文件中，MapStatus封装了Shuffle文件地址和计算结果的大小
    // 后面会对这个MapStatus进行序列化，返回给对应Executor的CoraseGrainedBackend上
    val value = try {

      /*
      	这是运行task最核心的方法
		这个task是ShuffleMapTask或ResultMapTask
      */
      val res = task.run(
        taskAttemptId = taskId,
        attemptNumber = taskDescription.attemptNumber,
        metricsSystem = env.metricsSystem)
      threwException = false
      // 返回任务执行的结果，就是MapStatus
      res
    } finally {
      val releasedLocks = env.blockManager.releaseAllLocksForTask(taskId)
      // 释放内存
      val freedMemory = taskMemoryManager.cleanUpAllAllocatedMemory()

      // 省略部分代码。。。
    }
  
  	// 到此，任务执行结束

    // 任务的结束时间
    val taskFinish = System.currentTimeMillis()
    val taskFinishCpu = if (threadMXBean.isCurrentThreadCpuTimeSupported) {
      threadMXBean.getCurrentThreadCpuTime
    } else 0L

    // If the task has been killed, let's fail it.
    task.context.killTaskIfInterrupted()

    // 为task执行后的结果创建序列化器
    val resultSer = env.serializer.newInstance()
    val beforeSerialization = System.currentTimeMillis()
    // 序列化Task执行后的结果，这个结果要返回给Driver
    val valueBytes = resultSer.serialize(value)
    val afterSerialization = System.currentTimeMillis()

    // 反序列化发生在两部分：首先是反序列化一个Task对象，包含了partition
    // 其次Task.run()反序列化RDD和function算子.

    //设置task运行时的一些指标，都会显示在SparkUI上
    task.metrics.setExecutorDeserializeTime(
      (taskStart - deserializeStartTime) + task.executorDeserializeTime)
    task.metrics.setExecutorDeserializeCpuTime(
      (taskStartCpu - deserializeStartCpuTime) + task.executorDeserializeCpuTime)
    // We need to subtract Task.run()'s deserialization time to avoid double-counting
    task.metrics.setExecutorRunTime((taskFinish - taskStart) - task.executorDeserializeTime)
    task.metrics.setExecutorCpuTime(
      (taskFinishCpu - taskStartCpu) - task.executorDeserializeCpuTime)
    task.metrics.setJvmGCTime(computeTotalGcTime() - startGCTime)
    task.metrics.setResultSerializationTime(afterSerialization - beforeSerialization)

    // Note: accumulator updates must be collected after TaskMetrics is updated
    val accumUpdates = task.collectAccumulatorUpdates()
    // 一个包含了Task结果与累加器的更新的TaskResult
    val directResult = new DirectTaskResult(valueBytes, accumUpdates)
    // 序列化TaskResult
    val serializedDirectResult = ser.serialize(directResult)
    // 计算序列化结果的大小
    val resultSize = serializedDirectResult.limit

    // directSend = sending directly back to the driver
    // 这一步主要是用于判断序列化结果的大小
    val serializedResult: ByteBuffer = {

      //如果执行结果序列化后的大小是否大于最大的限制大小（可配置，默认是1G），如果大于最大的大小，那么直接丢弃它
      if (maxResultSize > 0 && resultSize > maxResultSize) {
        logWarning(s"Finished $taskName (TID $taskId). Result is larger than maxResultSize " +
          s"(${Utils.bytesToString(resultSize)} > ${Utils.bytesToString(maxResultSize)}), " +
          s"dropping it.")
        ser.serialize(new IndirectTaskResult[Any](TaskResultBlockId(taskId), resultSize))
      } else if (resultSize > maxDirectResultSize) {
        val blockId = TaskResultBlockId(taskId)
        env.blockManager.putBytes(
          blockId,
          new ChunkedByteBuffer(serializedDirectResult.duplicate()),
          StorageLevel.MEMORY_AND_DISK_SER)
        logInfo(
          s"Finished $taskName (TID $taskId). $resultSize bytes result sent via BlockManager)")
        ser.serialize(new IndirectTaskResult[Any](blockId, resultSize))
      } else {
        logInfo(s"Finished $taskName (TID $taskId). $resultSize bytes result sent to driver")
        serializedDirectResult
      }
    }

    setTaskFinishedAndClearInterruptStatus()
    // 向Driver(其实是Executor所在的CoarseGrainedExecutorBackend)发送对应的task执行结果和执行状态
    execBackend.statusUpdate(taskId, TaskState.FINISHED, serializedResult)

  } catch {
    
    // 这里省略了一堆异常捕获。。。

  } finally {
  	// 最后总缓冲中移除该task
    runningTasks.remove(taskId)
  }
}

/* 五、Executor的updateDependencies()方法，该方法的作用是通过网络通信， 
   获取task依赖的文件、资源、jar包，比如hadoop的依赖文件 */

private def updateDependencies(newFiles: Map[String, Long], newJars: Map[String, Long]) {
	// 获取hadoop配置文件
	lazy val hadoopConf = SparkHadoopUtil.get.newConfiguration(conf)
	//同步代码块，因为在CoarseGrainedExecutorBackend进程中运行多个线程，
    //来执行不同的Task那么多个线程访问同一个资源，就会出现线程安全问题，
    //所以为了避免数据同步问题，加上同步到代码块
	synchronized {
		// 遍历要拉取的文件
		for ((name, timestamp) <- newFiles if currentFiles.getOrElse(name, -1L) < timestamp) {
			logInfo("Fetching " + name + " with timestamp " + timestamp)
			// 通过Utils.fetchFile方法，利用网络通信拉取依赖文件到本地的spark目录
			Utils.fetchFile(name, new File(SparkFiles.getRootDirectory()), conf,
			  env.securityManager, hadoopConf, timestamp, useCache = !isLocal)
			currentFiles(name) = timestamp
		}
		// 遍历拉取的jar
		for ((name, timestamp) <- newJars) {
			val localName = new URI(name).getPath.split("/").last
			val currentTimeStamp = currentJars.get(name)
			  .orElse(currentJars.get(localName))
			  .getOrElse(-1L)
			if (currentTimeStamp < timestamp) {
			  logInfo("Fetching " + name + " with timestamp " + timestamp)
			  // 拉取jar包到本地spark目录
			  Utils.fetchFile(name, new File(SparkFiles.getRootDirectory()), conf,
			    env.securityManager, hadoopConf, timestamp, useCache = !isLocal)
			  currentJars(name) = timestamp
			  // Add it to our class loader
			  val url = new File(SparkFiles.getRootDirectory(), localName).toURI.toURL
			  if (!urlClassLoader.getURLs().contains(url)) {
			    logInfo("Adding " + url + " to class loader")
			    urlClassLoader.addURL(url)
			  }
			}
		}
	}
}

  /* 六、Task里的run()方法，也就是执行task所需准备工作的结尾 */

  /**
	* Called by [[org.apache.spark.executor.Executor]] to run this task.
	* executor.launch() -> TaskRunner.run() -> task.run()  
	*
	* @param taskAttemptId an identifier for this task attempt that is unique within a SparkContext.
	* @param attemptNumber how many times this task has been attempted (0 for the first attempt)
	* @return the result of the task along with updates of Accumulators.
	*/
final def run(
      taskAttemptId: Long,
      attemptNumber: Int,
      metricsSystem: MetricsSystem): T = {
    SparkEnv.get.blockManager.registerTask(taskAttemptId)
    // Task执行的上下文，封装了task执行所需要的数据
    context = new TaskContextImpl(
      stageId,
      partitionId,
      taskAttemptId,
      attemptNumber,
      taskMemoryManager,
      localProperties,
      metricsSystem,
      metrics)
    TaskContext.setTaskContext(context)
    taskThread = Thread.currentThread()

    if (_reasonIfKilled != null) {
      kill(interruptThread = false, _reasonIfKilled)
    }

    new CallerContext(
      "TASK",
      SparkEnv.get.conf.get(APP_CALLER_CONTEXT),
      appId,
      appAttemptId,
      jobId,
      Option(stageId),
      Option(stageAttemptId),
      Option(taskAttemptId),
      Option(attemptNumber)).setCurrentContext()

    try {

      // 调用runTask方法，因为runTask是一个抽象方法，所以它的处理逻辑都是基于子类来实现的
      // 因为Task的子类有两个，一个是ShuffleMapTask,另个一是ResultTask，如果想看具体的Task
      // 执行程序，就需要到这两个子类去解析具体的处理逻辑
      // 返回的结果是一个MapStatus
      runTask(context)
    } catch {
      
      // 省略异常处理。。

    } finally {
      try {
        context.markTaskCompleted(None)
      } finally {
        try {
          Utils.tryLogNonFatalError {
            // Release memory used by this thread for unrolling blocks
            SparkEnv.get.blockManager.memoryStore.releaseUnrollMemoryForThisTask(MemoryMode.ON_HEAP)
            SparkEnv.get.blockManager.memoryStore.releaseUnrollMemoryForThisTask(
              MemoryMode.OFF_HEAP)
            // Notify any tasks waiting for execution memory to be freed to wake up and try to
            // acquire memory again. This makes impossible the scenario where a task sleeps forever
            // because there are no other tasks left to notify it. Since this is safe to do but may
            // not be strictly necessary, we should revisit whether we can remove this in the
            // future.
            val memoryManager = SparkEnv.get.memoryManager
            memoryManager.synchronized { memoryManager.notifyAll() }
          }
        } finally {
          // Though we unset the ThreadLocal here, the context member variable itself is still
          // queried directly in the TaskRunner to check for FetchFailedExceptions.
          TaskContext.unset()
        }
      }
    }
}


/* 七、ShuffleMapTask原理解析: 将RDD的每个partition拆分成多个bucket存储桶或存储区) */
/* 
	ShuffleMapTask中的runTask()方法
	该方法的作用是执行Task，然后将结果返回给调度器
	其中MapStatus封装了块管理器(BlockManager)的地址，以及每个reduce的输出大小，以便传输reduce任务
*/
override def runTask(context: TaskContext): MapStatus = {
    val threadMXBean = ManagementFactory.getThreadMXBean
    val deserializeStartTime = System.currentTimeMillis()
    val deserializeStartCpuTime = if (threadMXBean.isCurrentThreadCpuTimeSupported) {
      threadMXBean.getCurrentThreadCpuTime
    } else 0L
    // 创建一个序列化器
    val ser = SparkEnv.get.closureSerializer.newInstance()

    // 反序列化广播变量以得到RDD(RDD是存放在广播变量中的)
    val (rdd, dep) = ser.deserialize[(RDD[_], ShuffleDependency[_, _, _])](
      ByteBuffer.wrap(taskBinary.value), Thread.currentThread.getContextClassLoader)
    _executorDeserializeTime = System.currentTimeMillis() - deserializeStartTime
    _executorDeserializeCpuTime = if (threadMXBean.isCurrentThreadCpuTimeSupported) {
      threadMXBean.getCurrentThreadCpuTime - deserializeStartCpuTime
    } else 0L

    var writer: ShuffleWriter[Any, Any] = null
    try {

      	// 获取ShuffleManager
		val manager = SparkEnv.get.shuffleManager
		// 使用ShuffleManager获取ShuffleWriter,ShuffleWriter的作用：
		// 将Task的计算结果持久化到shuffle文件中，作为子stage的输入
		writer = manager.getWriter[Any, Any](dep.shuffleHandle, partitionId, context)
		// rdd.iterator(partition, context)方法里会执行我们自己编写的业务逻辑代码
		writer.write(rdd.iterator(partition, context).asInstanceOf[Iterator[_ <: Product2[Any, Any]]])
		// 关闭writer，将元数据写入MapStatus,然后返回
		writer.stop(success = true).get
    } catch {
      case e: Exception =>
        try {
          if (writer != null) {
            writer.stop(success = false)
          }
        } catch {
          case e: Exception =>
            log.debug("Could not stop writer", e)
        }
        throw e
    }
}

/* 
  	八、RDD中的方法 
*/
final def iterator(split: Partition, context: TaskContext): Iterator[T] = {
   	//判断存储级别，如果存储不为None，那么就会从缓存中取出Partition
    if (storageLevel != StorageLevel.NONE) {
      getOrCompute(split, context)
    } else {
      //Partition没有缓存，就调用这个方法
      computeOrReadCheckpoint(split, context)
    }
}

/* 计算RDD的partition,如果RDd正在checkpoint,就从checkpoint中读取 */
private[spark] def computeOrReadCheckpoint(split: Partition, context: TaskContext): Iterator[T] ={

	if (isCheckpointedAndMaterialized) {
	  firstParent[T].iterator(split, context)
	} else {
	  //调用RDD的compute方法开始执行Task
	  compute(split, context)
	}
}





