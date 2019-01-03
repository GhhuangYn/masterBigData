 private[scheduler] def handleJobSubmitted(jobId: Int,
      finalRDD: RDD[_],
      func: (TaskContext, Iterator[_]) => _,
      partitions: Array[Int],
      callSite: CallSite,
      listener: JobListener,
      properties: Properties) {
  
    var finalStage: ResultStage = null
    try {
      HadoopRDD whose underlying HDFS files have been deleted.
      // 1.使用触发job的最后一个RDD，创建一个最后一个stage
      // 这个finalStage实际上已经包含了整个DAG图,通过它的parents属性可以一层层网上追踪
      finalStage = createResultStage(finalRDD, func, partitions, jobId, callSite)
    } catch {
      case e: Exception =>
        logWarning("Creating new stage failed due to exception - job: " + jobId, e)
        listener.jobFailed(e)
        return
    }

    // 2.用finalStage创建一个Job，也就是说这个Job的最后一个stage就是前面创建的finalStage
    val job = new ActiveJob(jobId, finalStage, callSite, listener, properties)
    clearCacheLocs()
    logInfo("Got job %s (%s) with %d output partitions".format(
      job.jobId, callSite.shortForm, partitions.length))
    logInfo("Final stage: " + finalStage + " (" + finalStage.name + ")")
    logInfo("Parents of final stage: " + finalStage.parents)
    logInfo("Missing parents: " + getMissingParentStages(finalStage))

    val jobSubmissionTime = clock.getTimeMillis()
    // 3.把job加入内存缓存中
    jobIdToActiveJob(jobId) = job
    activeJobs += job
    finalStage.setActiveJob(job)
    val stageIds = jobIdToStageIds(jobId).toArray
    val stageInfos = stageIds.flatMap(id => stageIdToStage.get(id).map(_.latestInfo))
    listenerBus.post(
      SparkListenerJobStart(job.jobId, jobSubmissionTime, stageInfos, properties))
    // 4.使用submitStage()方法提交finalStage
    // 这个方法的调用其实会导致第一个stage的提交
    // 并且会导致其他所有的stage放入waitingStages中
    submitStage(finalStage)
  }

   /** Submits stage, but first recursively submits any missing parents. */
  private def submitStage(stage: Stage) {
    val jobId = activeJobForStage(stage)
    if (jobId.isDefined) {
      logDebug("submitStage(" + stage + ")")
      if (!waitingStages(stage) && !runningStages(stage) && !failedStages(stage)) {
        // 获取某个stage的父stage
        val missing = getMissingParentStages(stage).sortBy(_.id)
        logDebug("missing: " + missing)
        // 这里其实会反复递归调用，直到最初的stage没有它没有父stage
        // 此时就会首先提交第一个stage(stage0)
        // 其余的stage，此时已经全部在waitingStages里面
        // 比如，首先提交的是finalStage，那么通过getMissingParentStages()可以获取到它的父stage(是一个shuffleMapStage)
        // 然后如果返回的missing不为空，就是说，finalStage有父stage，然后把这个父stage又提交到submitStage
        // 递归的提交所有的stage
        // 最终的结果提交顺序是： 从左往右依次提交stage(因为是递归的)

        // 这里是递归的出口
        if (missing.isEmpty) {
          logInfo("Submitting " + stage + " (" + stage.rdd + "), which has no missing parents")
          // 如果没有父stage(也就是第一个stage)，那么就开始提交任务了
          // 然后递归就会一步步往上弹出
          submitMissingTasks(stage, jobId.get)
        } else {
          // 递归调用submit()方法，提交父stage
          // 这里的递归是stage划分算法的推动者和精髓
          for (parent <- missing) {
            submitStage(parent)
          }
          // 当递归跳出的时候，后进入submitStage的stage先弹出，因此父stage先进入等待队列中
          // 由于第一个stage会被提交执行，其他的stage会被加入到等待队列中等待执行
          // 因此stage按从左往右的顺序进入等待队列stage1,stage2，..., resultStage
          waitingStages += stage
        }
      }
    } else {
      abortStage(stage, "No active job for stage " + stage.id, None)
    }
  }

  /* 
    获取某个stage的父stage
    对一个stage，如果它的最后一个rdd的所有依赖都是窄依赖，那么就不会创建任何新的stage
    但是只要发现这个stage的RDD宽依赖了某个RDD，那么就用宽依赖的那个RDD，创建一个新的stage并且立即将新的stage返回
  */
  private def getMissingParentStages(stage: Stage): List[Stage] = {
    val missing = new HashSet[Stage]
    val visited = new HashSet[RDD[_]]
    // We are manually maintaining a stack here to prevent StackOverflowError
    // caused by recursively visiting
    val waitingForVisit = new Stack[RDD[_]]

    def visit(rdd: RDD[_]) {
      if (!visited(rdd)) {
        visited += rdd
        // getCacheLocs函数的使用，主要作用就是把当前的RDD进行cache放到内存来加快访问的速度。
        // 具体操作就是如果rdd已经cache就返回cache的位置，否则就执行cache操作：
        val rddHasUncachedPartitions = getCacheLocs(rdd).contains(Nil)
        if (rddHasUncachedPartitions) {
          // 遍历RDD的依赖(分为OneToOneDependency和ShuffleDependency)
          for (dep <- rdd.dependencies) {
            dep match {
              // 如果是宽依赖，那么使用宽依赖的那个RDD创建一个新的stage
              // 默认最后一个是ResultStage，其他事shuffleMapStage
              case shufDep: ShuffleDependency[_, _, _] =>
                val mapStage = getOrCreateShuffleMapStage(shufDep, stage.firstJobId)
                if (!mapStage.isAvailable) {
                  missing += mapStage
                }
              // 如果是窄依赖，就把这个RDD压入栈中(相当于加入该stage)
              case narrowDep: NarrowDependency[_] =>
                waitingForVisit.push(narrowDep.rdd)
            }
          }
        }
      }
    }

    // 首先往栈中推入了stage的最后一个rdd
    waitingForVisit.push(stage.rdd)

    // 循环
    while (waitingForVisit.nonEmpty) {
      // 弹出栈中的最后一个RDD，并将它传入visit方法
      visit(waitingForVisit.pop())
    }
    missing.toList
  }

  /*
    stage算法总结
    1.从finalStage倒推
    2.通过宽依赖来进行新的stage划分
    3.使用递归，优先提交父stage
    
  */



  /** 提交stage，为stage创建一批task，task数量与partition数量相同 */
  private def submitMissingTasks(stage: Stage, jobId: Int) {
    logDebug("submitMissingTasks(" + stage + ")")

    // 获取partition索引
    // First figure out the indexes of partition ids to compute.
    val partitionsToCompute: Seq[Int] = stage.findMissingPartitions()

    // Use the scheduling pool, job group, description, etc. from an ActiveJob associated
    // with this Stage
    val properties = jobIdToActiveJob(jobId).properties

    // 将stage加入runningStages队列
    runningStages += stage
    // SparkListenerStageSubmitted should be posted before testing whether tasks are
    // serializable. If tasks are not serializable, a SparkListenerStageCompleted event
    // will be posted, which should always come after a corresponding SparkListenerStageSubmitted
    // event.
    stage match {
      case s: ShuffleMapStage =>
        outputCommitCoordinator.stageStart(stage = s.id, maxPartitionId = s.numPartitions - 1)
      case s: ResultStage =>
        outputCommitCoordinator.stageStart(
          stage = s.id, maxPartitionId = s.rdd.partitions.length - 1)
    }
    val taskIdToLocations: Map[Int, Seq[TaskLocation]] = try {
      stage match {
        case s: ShuffleMapStage =>
          partitionsToCompute.map { id => (id, getPreferredLocs(stage.rdd, id))}.toMap
        case s: ResultStage =>
          partitionsToCompute.map { id =>
            val p = s.partitions(id)
            (id, getPreferredLocs(stage.rdd, p))
          }.toMap
      }
    } catch {
      case NonFatal(e) =>
        stage.makeNewStageAttempt(partitionsToCompute.size)
        listenerBus.post(SparkListenerStageSubmitted(stage.latestInfo, properties))
        abortStage(stage, s"Task creation failed: $e\n${Utils.exceptionString(e)}", Some(e))
        runningStages -= stage
        return
    }

    stage.makeNewStageAttempt(partitionsToCompute.size, taskIdToLocations.values.toSeq)
    listenerBus.post(SparkListenerStageSubmitted(stage.latestInfo, properties))

    // TODO: Maybe we can keep the taskBinary in Stage to avoid serializing it multiple times.
    // Broadcasted binary for the task, used to dispatch tasks to executors. Note that we broadcast
    // the serialized copy of the RDD and for each task we will deserialize it, which means each
    // task gets a different copy of the RDD. This provides stronger isolation between tasks that
    // might modify state of objects referenced in their closures. This is necessary in Hadoop
    // where the JobConf/Configuration object is not thread-safe.
    var taskBinary: Broadcast[Array[Byte]] = null
    try {
      // For ShuffleMapTask, serialize and broadcast (rdd, shuffleDep).
      // For ResultTask, serialize and broadcast (rdd, func).
      val taskBinaryBytes: Array[Byte] = stage match {
        case stage: ShuffleMapStage =>
          JavaUtils.bufferToArray(
            closureSerializer.serialize((stage.rdd, stage.shuffleDep): AnyRef))
        case stage: ResultStage =>
          JavaUtils.bufferToArray(closureSerializer.serialize((stage.rdd, stage.func): AnyRef))
      }

      taskBinary = sc.broadcast(taskBinaryBytes)
    } catch {
      // In the case of a failure during serialization, abort the stage.
      case e: NotSerializableException =>
        abortStage(stage, "Task not serializable: " + e.toString, Some(e))
        runningStages -= stage

        // Abort execution
        return
      case NonFatal(e) =>
        abortStage(stage, s"Task serialization failed: $e\n${Utils.exceptionString(e)}", Some(e))
        runningStages -= stage
        return
    }

    // 为stage创建指定数量的task
    val tasks: Seq[Task[_]] = try {
      val serializedTaskMetrics = closureSerializer.serialize(stage.latestInfo.taskMetrics).array()
      stage match {
        case stage: ShuffleMapStage =>
          stage.pendingPartitions.clear()
          partitionsToCompute.map { id =>

            // 给每个partition都创建一个task，并为每个task计算最佳位置
            // 这里是重点
            val locs = taskIdToLocations(id)
            // 获得partition
            val part = stage.rdd.partitions(id)
            stage.pendingPartitions += id
            // 除了finalStage是resultStage之外，其余的都是shuflleMapStage
            // 因此创建shuffleMapTask
            new ShuffleMapTask(stage.id, stage.latestInfo.attemptId,
              taskBinary, part, locs, properties, serializedTaskMetrics, Option(jobId),
              Option(sc.applicationId), sc.applicationAttemptId)
          }

        case stage: ResultStage =>
          partitionsToCompute.map { id =>
            val p: Int = stage.partitions(id)
            val part = stage.rdd.partitions(p)
            val locs = taskIdToLocations(id)
            new ResultTask(stage.id, stage.latestInfo.attemptId,
              taskBinary, part, locs, id, properties, serializedTaskMetrics,
              Option(jobId), Option(sc.applicationId), sc.applicationAttemptId)
          }
      }
    } catch {
      case NonFatal(e) =>
        abortStage(stage, s"Task creation failed: $e\n${Utils.exceptionString(e)}", Some(e))
        runningStages -= stage
        return
    }

    if (tasks.size > 0) {
      logInfo(s"Submitting ${tasks.size} missing tasks from $stage (${stage.rdd}) (first 15 " +
        s"tasks are for partitions ${tasks.take(15).map(_.partitionId)})")
      taskScheduler.submitTasks(new TaskSet(
        tasks.toArray, stage.id, stage.latestInfo.attemptId, jobId, properties))
      stage.latestInfo.submissionTime = Some(clock.getTimeMillis())
    } else {
      // Because we posted SparkListenerStageSubmitted earlier, we should mark
      // the stage as completed here in case there are no tasks to run
      markStageAsFinished(stage, None)

      val debugString = stage match {
        case stage: ShuffleMapStage =>
          s"Stage ${stage} is actually done; " +
            s"(available: ${stage.isAvailable}," +
            s"available outputs: ${stage.numAvailableOutputs}," +
            s"partitions: ${stage.numPartitions})"
        case stage : ResultStage =>
          s"Stage ${stage} is actually done; (partitions: ${stage.numPartitions})"
      }
      logDebug(debugString)

      submitWaitingChildStages(stage)
    }
  }

  /*
    计算每个task对应的partition的最佳位置
    就是从stage的最后一个RDD开始去找哪个RDD的partition被cache了，或者checkpoint
    task的最佳位置，就是缓存/checkpoint的partition
    这样的话，task就在该节点上执行，不需要计算之前的RDD
  */
  private def getPreferredLocsInternal(
      rdd: RDD[_],
      partition: Int,
      visited: HashSet[(RDD[_], Int)]): Seq[TaskLocation] = {
    // If the partition has already been visited, no need to re-visit.
    // This avoids exponential path exploration.  SPARK-695
    if (!visited.add((rdd, partition))) {
      // Nil has already been returned for previously visited partitions.
      return Nil
    }
    // If the partition is cached, return the cache locations
    val cached = getCacheLocs(rdd)(partition)
    if (cached.nonEmpty) {
      return cached
    }
    // If the RDD has some placement preferences (as is the case for input RDDs), get those
    val rddPrefs = rdd.preferredLocations(rdd.partitions(partition)).toList
    if (rddPrefs.nonEmpty) {
      return rddPrefs.map(TaskLocation(_))
    }

    // If the RDD has narrow dependencies, pick the first partition of the first narrow dependency
    // that has any placement preferences. Ideally we would choose based on transfer sizes,
    // but this will do for now.
    rdd.dependencies.foreach {
      case n: NarrowDependency[_] =>
        for (inPart <- n.getParents(partition)) {
          val locs = getPreferredLocsInternal(n.rdd, inPart, visited)
          if (locs != Nil) {
            return locs
          }
        }

      case _ =>
    }

    Nil
  }