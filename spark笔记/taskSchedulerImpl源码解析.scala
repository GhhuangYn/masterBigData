
/******************************TaskSchedulerImpl 提交任务*******************************/

/*
  提交任务的入口
*/
override def submitTasks(taskSet: TaskSet) {
    val tasks = taskSet.tasks
    this.synchronized {

      // 首先会创建一个TaskSetManager，它主要负责调度TaskSet中的Task
      // 默认的失败重试次数为4次，使用spark.task.maxTaskFailures来设置
      val manager = createTaskSetManager(taskSet, maxTaskFailures)
      val stage = taskSet.stageId
      val stageTaskSets =
        taskSetsByStageIdAndAttempt.getOrElseUpdate(stage, new HashMap[Int, TaskSetManager])
      stageTaskSets(taskSet.stageAttemptId) = manager
      val conflictingTaskSet = stageTaskSets.exists { case (_, ts) =>
        ts.taskSet != taskSet && !ts.isZombie
      }
      if (conflictingTaskSet) {
        throw new IllegalStateException(s"more than one active taskSet for stage $stage:" +
          s" ${stageTaskSets.toSeq.map{_._2.taskSet.id}.mkString(",")}")
      }

      //然后把TaskSetManager交由Pool的队列进行统一调度
      //SchedulableBuilder是一个trait，是一个调度树的接口，有FIFO,FAIR两种方式调度资源
      schedulableBuilder.addTaskSetManager(manager, manager.taskSet.properties)

      if (!isLocal && !hasReceivedTask) {
        starvationTimer.scheduleAtFixedRate(new TimerTask() {
          override def run() {
            if (!hasLaunchedTask) {
              logWarning("Initial job has not accepted any resources; " +
                "check your cluster UI to ensure that workers are registered " +
                "and have sufficient resources")
            } else {
              this.cancel()
            }
          }
        }, STARVATION_TIMEOUT_MS, STARVATION_TIMEOUT_MS)
      }
      hasReceivedTask = true
    }

    // sparkContext原理剖析时，创建taskScheduler时，就是为taskSchedulerImp创建一个
    // SchedulerBackend,这里就使用到了
    // 这个backend是负责创建appClient的，向Master注册application
    backend.reviveOffers()
  }

  /* 底层调用StandaloneSchedulerBackend(schedulerBackend进行处理)*/

  // 这个是StandaloneSchedulerBackend的父类CoarseGrainedSchedulerBackend中的方法
  override def reviveOffers() {
     //向自己内部的一个通讯类（DriverEndpoint）发送消息
     driverEndpoint.send(ReviveOffers)
  }
  // 然后自己接收到消息，进行模式匹配
  override def receive: PartialFunction[Any, Unit] = {
        case ReviveOffers => //接收消息
          makeOffers()
  }

  /*
    这个方法的作用是分配
  */
  private def makeOffers() {
    // 确保task加载的时候没有executor被杀死
    val taskDescs = CoarseGrainedSchedulerBackend.this.synchronized {
      // executorDataMap保存的是每个executorID对用的executorInfo信息的映射，过滤出存活可用的executor
      val activeExecutors = executorDataMap.filterKeys(executorIsAlive)
      // 从ExecutorData类中抽出了3个属性封装成一个临时对象，代表每个executor的可用资源
      // WorkerOffer类代表的是executor的可用资源
      // WorkerOffer(executorId, host, cores)
      val workOffers = activeExecutors.map { case (id, executorData) =>
        new WorkerOffer(id, executorData.executorHost, executorData.freeCores)
      }.toIndexedSeq

      scheduler.resourceOffers(workOffers)
    }

    // 分配task到executor之后，由executor启动并执行task
    if (!taskDescs.isEmpty) {
      launchTasks(taskDescs)
    }
  }

  /**
   * Called by cluster manager to offer resources on slaves. We respond by asking our active task
   * sets for tasks in order of priority. We fill each node with tasks in a round-robin manner so
   * that tasks are balanced across the cluster.
      由cluster manager响应，在slaves上分配资源，按task的优先级顺序响应任务
      我们用循环的方式把task分配到每个节点上，因此集群钟的任务是很均衡的
   */
  def resourceOffers(offers: IndexedSeq[WorkerOffer]): Seq[Seq[TaskDescription]] = synchronized {
    // 标记每个存活的从节点，并记录它的主机名
    // 总是检测新的增加的executor
    var newExecAvail = false
    // 遍历可用的executor资源，加入到缓存当中
    for (o <- offers) {
      // hostToExecutors存放的是本地缓存映射的集合
      // 如果它不包含当前循环到的executor，就加入当前的缓存中
      if (!hostToExecutors.contains(o.host)) {
        hostToExecutors(o.host) = new HashSet[String]()
      }

      // 如果当前的executor并不在运行中，那么就把exec加入到每个需要的缓存结构中
      if (!executorIdToRunningTaskIds.contains(o.executorId)) {
        hostToExecutors(o.host) += o.executorId
        executorAdded(o.executorId, o.host)
        executorIdToHost(o.executorId) = o.host
        executorIdToRunningTaskIds(o.executorId) = HashSet[Long]()
        newExecAvail = true
      }

      // 添加到机架存放的主机列表
      for (rack <- getRackForHost(o.host)) {
        hostsByRack.getOrElseUpdate(rack, new HashSet[String]()) += o.host
      }
    }

    // 在分配资源之前，需要过滤掉所有黑名单中已经过期的节点
    blacklistTrackerOpt.foreach(_.applyBlacklistTimeout())

    val filteredOffers = blacklistTrackerOpt.map { blacklistTracker =>
      offers.filter { offer =>
        !blacklistTracker.isNodeBlacklisted(offer.host) &&
          !blacklistTracker.isExecutorBlacklisted(offer.executorId)
      }
    }.getOrElse(offers)

    // 随机打乱资源,避免把task大量集中在少量的worker上启动
    val shuffledOffers = shuffleOffers(filteredOffers)
    
    // 创建一个二维数组，代表的是每个executor中包含一个可用cpu core个数的长度的task列表
    // 就是说每个executor最多可以以运行多少个task
    val tasks = shuffledOffers.map(o => new ArrayBuffer[TaskDescription](o.cores))

    // 把WorkOffer的属性cores单独拿出来成为一个集合
    val availableCpus = shuffledOffers.map(o => o.cores).toArray
    // 从rootPool取出排序的taskSet
    // TaskScheduler初始化的时候，创建完TaskScehduluer、SchuedulerBackend后
    // 执行一个initialize()方法，其实会创建一个调度池
    // 所有提交的taskSet，会放入这个调度池
    // 然后在执行task分配算法的时候，会从这个调度池中取出排好队的taskSet
    val sortedTaskSets = rootPool.getSortedTaskSetQueue
    for (taskSet <- sortedTaskSets) {
      logDebug("parentName: %s, name: %s, runningTasks: %s".format(
        taskSet.parent.name, taskSet.name, taskSet.runningTasks))
      if (newExecAvail) {
        taskSet.executorAdded()
      }
    }

    /**************任务分配算法的核心**************/

    // 到此为止，准备工作完成
    // 拿到了shuffledOffers(可用的executor资源)，tasks二维数组(存放task)，
    // availableCpus(可用cpu数量)，sortedTaskSets(经排序的taskSet)

    // 遍历所有的taskSet，然后分配到每个节点
    // 本地化级别：PROCESS_LOCAL,进程本地化RDD的partition和task进入同一个executor内，速度最快
    //            NODE_LOCAL,RDD的partition和task不在一个executor中，不在一个进程，但是在一个worker节点中
    //            NO_PREF,没有本地化级别
    //            RACK_LOCAL,机架本地化 RDD的partition和task至少在一个机架上
    //            ANY，任意的本地化级别

    // 对于每一个taskSet，从最好的本地化级别PROCESS_LOCAL开始循环
    for (taskSet <- sortedTaskSets) {
      var launchedAnyTask = false
      var launchedTaskAtCurrentMaxLocality = false
      for (currentMaxLocality <- taskSet.myLocalityLevels) {
        do {

          //尝试使用当前的本地化级别判断TaskSet中的task能否在executor中启动
          // 如果失败就退出do-while循环，进行下一个本地化级别的尝试
          launchedTaskAtCurrentMaxLocality = resourceOfferSingleTaskSet(
            taskSet, currentMaxLocality, shuffledOffers, availableCpus, tasks)
          launchedAnyTask |= launchedTaskAtCurrentMaxLocality
        } while (launchedTaskAtCurrentMaxLocality)
      }
      // taskSet全部启动失败，则终止taskSet
      if (!launchedAnyTask) {
        taskSet.abortIfCompletelyBlacklisted(hostToExecutors)
      }
    }

    if (tasks.size > 0) {
      hasLaunchedTask = true
    }
    // 最后到了这一步，task数组中包含了每个executor可以运行哪些tasks
    return tasks
  }

  /* 判别taskSet的task能否以当前的本地化级别在executor中启动 */
  private def resourceOfferSingleTaskSet(
      taskSet: TaskSetManager,
      maxLocality: TaskLocality,
      shuffledOffers: Seq[WorkerOffer],
      availableCpus: Array[Int],
      tasks: IndexedSeq[ArrayBuffer[TaskDescription]]) : Boolean = {
    var launchedTask = false
    
    // 遍历所有的executor
    for (i <- 0 until shuffledOffers.size) {
      val execId = shuffledOffers(i).executorId
      val host = shuffledOffers(i).host

      // 如果当前的executor的free core至少大于等于每个task要使用core的数量，默认1
      if (availableCpus(i) >= CPUS_PER_TASK) {
        try {

          // 调用taskManager的resouceOffer()方法，这个方法很复杂，主要是通过等待时间判断
          // 判别在这个executor上，使用当前的本地化级别可以启动taskSet中的哪些task
          // 如果可以启动，就遍历返回的task集合，把task加入到该executor所属的tasks这个二维数组
          for (task <- taskSet.resourceOffer(execId, host, maxLocality)) {
            val tid = task.taskId
            tasks(i) += task
            taskIdToTaskSetManager(tid) = taskSet
            taskIdToExecutorId(tid) = execId
            executorIdToRunningTaskIds(execId).add(tid)
            availableCpus(i) -= CPUS_PER_TASK
            assert(availableCpus(i) >= 0)
            launchedTask = true
          }
        } catch {
          case e: TaskNotSerializableException =>
            logError(s"Resource offer failed, task set ${taskSet.name} was not serializable")
            // Do not offer resources for this task, but don't throw an error to allow other
            // task sets to be submitted.
            return launchedTask
        }
      }
    }
    return launchedTask
  }

  // 至此，需要得到的tasks已经拿到
  // 也就是说在哪些executor中应该启动哪些task
  // 然后就开始在executor上启动task

  // Launch tasks returned by a set of resource offers
  private def launchTasks(tasks: Seq[Seq[TaskDescription]]) {

    // 所有的tasks平摊，使二维数组变成一维数组
    for (task <- tasks.flatten) {

      // 序列化
      val serializedTask = TaskDescription.encode(task)

      // 省略解析。。。
      if (serializedTask.limit >= maxRpcMessageSize) {
        scheduler.taskIdToTaskSetManager.get(task.taskId).foreach { taskSetMgr =>
          try {
            var msg = "Serialized task %s:%d was %d bytes, which exceeds max allowed: " +
              "spark.rpc.message.maxSize (%d bytes). Consider increasing " +
              "spark.rpc.message.maxSize or using broadcast variables for large values."
            msg = msg.format(task.taskId, task.index, serializedTask.limit, maxRpcMessageSize)
            taskSetMgr.abort(msg)
          } catch {
            case e: Exception => logError("Exception in error callback", e)
          }
        }
      }
      else {

        // 因为前面task在哪些exec中启动已经计算好了，所以task中有exec的信息(execId)
        // 通过ID找到缓存中的executor
        val executorData = executorDataMap(task.executorId)
        // 减去资源，通过"spark.task.cpus"设置，默认为1，即默认1个cpu core对应一个task
        executorData.freeCores -= scheduler.CPUS_PER_TASK

        // 发送信息，启动task
        // 这个消息终端的实体是executor包下的CoarseGrainedExecutorBackend
        executorData.executorEndpoint.send(LaunchTask(new SerializableBuffer(serializedTask)))
      }
    }
  }