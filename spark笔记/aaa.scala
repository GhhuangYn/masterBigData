 /**
    获取或创建给定RDD的父Stages列表
    * Get or create the list of parent stages for a given RDD.  The new Stages will be created with
   * the provided firstJobId.
   */
  private def getOrCreateParentStages(rdd: RDD[_], firstJobId: Int): List[Stage] = {
    // 遍历该RDD的所有直接依赖
    getShuffleDependencies(rdd).map { shuffleDep =>
      // 其实一般只有一个RDD只有一个依赖，但是也有多个依赖的，比如coGroupBy产生的RDD就会有多个依赖
      // 然后通过依赖和jobId,获取或创建一个shuffleMapStage
      getOrCreateShuffleMapStage(shuffleDep, firstJobId)
    }.toList
  }

/**
   * 返回与给定RDD直接相关的shuffle依赖(宽依赖)
   * 
     This function will not return more distant ancestors.  For example, if C has a shuffle
   * dependency on B which has a shuffle dependency on A:
   *
   * A <-- B <-- C
   *
   * 方法只返回 B --> C 的依赖
   *
   * This function is scheduler-visible for the purpose of unit testing.
   */
  private[scheduler] def getShuffleDependencies(
      rdd: RDD[_]): HashSet[ShuffleDependency[_, _, _]] = {
    // 用于存储ResultStage的父Stage
    val parents = new HashSet[ShuffleDependency[_, _, _]]
    // 存储已经遍历过的RDD
    val visited = new HashSet[RDD[_]]
    val waitingForVisit = new Stack[RDD[_]]
    // 把给定的RDD放入栈中
    waitingForVisit.push(rdd)
    //如果栈不为空
    while (waitingForVisit.nonEmpty) {
      // 弹出栈顶元素
      val toVisit = waitingForVisit.pop()
      // 如果该RDD还没有被遍历过 
      if (!visited(toVisit)) {
        visited += toVisit
        // 遍历该RDD所有的依赖关系
        toVisit.dependencies.foreach {
          // 如果是宽依赖就加入parents队列中
          case shuffleDep: ShuffleDependency[_, _, _] =>
            parents += shuffleDep
          // 否则把这个dep对应的上个RDD放到栈中
          // 比如当前的RDD-A 当前的依赖depAB A依赖于RDD-B
          // 那么就把RDD-B放入栈中
          // 直到遇到宽依赖，循环就结束了，输出parents
          case dependency =>
            waitingForVisit.push(dependency.rdd)
        }
      }
    }
    // 最后返回给定RDD的所有宽依赖
    parents
  }

  /**
   * 通过宽依赖获取一个shuffleMapStage
     获取一个shuffleMapStage，如果在shuffleIdToMapStage存在的话。
     如果不存在，那么此方法会为祖先ShuffleMapStage创建一个ShuffleMapStage
     Gets a shuffle map stage if one exists in shuffleIdToMapStage. Otherwise, if the
   * shuffle map stage doesn't already exist, this method will create the shuffle map stage in
   * addition to any missing ancestor shuffle map stages.
   */
  private def getOrCreateShuffleMapStage(
      shuffleDep: ShuffleDependency[_, _, _],
      firstJobId: Int): ShuffleMapStage = {
    shuffleIdToMapStage.get(shuffleDep.shuffleId) match {

      // 如果shuffleIdToMapStage已经有当前的shuffleDep，就直接返回
      case Some(stage) =>
        stage

      case None =>
        // 为所有的祖先宽依赖创建stage
        // Create stages for all missing ancestor shuffle dependencies.
        getMissingAncestorShuffleDependencies(shuffleDep.rdd).foreach { dep =>
          // Even though getMissingAncestorShuffleDependencies only returns shuffle dependencies
          // that were not already in shuffleIdToMapStage, it's possible that by the time we
          // get to a particular dependency in the foreach loop, it's been added to
          // shuffleIdToMapStage by the stage creation process for an earlier dependency. See
          // SPARK-13902 for more information.
          if (!shuffleIdToMapStage.contains(dep.shuffleId)) {
            createShuffleMapStage(dep, firstJobId)
          }
        }
        // Finally, create a stage for the given shuffle dependency.
        createShuffleMapStage(shuffleDep, firstJobId)
    }
  }

  /*
    获得祖先的依赖关系为宽依赖的依赖(从右向左查找)
    然后一次创建并向shuffleToMapStage中注册ShuffleStage(从左向右创建)
  */
  private def getMissingAncestorShuffleDependencies(
      rdd: RDD[_]): Stack[ShuffleDependency[_, _, _]] = {
    val ancestors = new Stack[ShuffleDependency[_, _, _]]
    val visited = new HashSet[RDD[_]]
    // We are manually maintaining a stack here to prevent StackOverflowError
    // caused by recursively visiting
    val waitingForVisit = new Stack[RDD[_]]
    waitingForVisit.push(rdd)
    while (waitingForVisit.nonEmpty) {
      val toVisit = waitingForVisit.pop()
      if (!visited(toVisit)) {
        visited += toVisit
        getShuffleDependencies(toVisit).foreach { shuffleDep =>
          if (!shuffleIdToMapStage.contains(shuffleDep.shuffleId)) {
            ancestors.push(shuffleDep)
            waitingForVisit.push(shuffleDep.rdd)
          } // Otherwise, the dependency and its ancestors have already been registered.
        }
      }
    }
    ancestors
  }

  /**
   * Creates a ShuffleMapStage that generates the given shuffle dependency's partitions. If a
   * previously run stage generated the same shuffle data, this function will copy the output
   * locations that are still available from the previous shuffle to avoid unnecessarily
   * regenerating data.
   */
  /*
    为给定的宽依赖的创建一个ShuffleMapStage
    如果前一个运行的stage产生相同的shuffle数据，这个函数将复制之前的数据而不会重新计算
  */
  def createShuffleMapStage(shuffleDep: ShuffleDependency[_, _, _], jobId: Int): ShuffleMapStage = {
    // 取出宽依赖的最后一个RDD,也就是最右的RDD
    val rdd = shuffleDep.rdd
    // Tasks的个数，由此可见，Stage的并行度是由该Stage内的最后一个RDD的partitions的个数所决定的
    val numTasks = rdd.partitions.length
    // 可以看到此处又是调用的getParentStagesAndId函数，然后重复上述的步骤
    val parents = getOrCreateParentStages(rdd, jobId)
    // 产生一个新的id，按dag图从左往右递增
    val id = nextStageId.getAndIncrement()
    // 创建一个ShuffleMapStage
    val stage = new ShuffleMapStage(id, rdd, numTasks, parents, jobId, rdd.creationSite, shuffleDep)
    // 添加到stageIdToStage集合中
    stageIdToStage(id) = stage
    shuffleIdToMapStage(shuffleDep.shuffleId) = stage
    updateJobIdStageIdMaps(jobId, stage)

    if (mapOutputTracker.containsShuffle(shuffleDep.shuffleId)) {
      // A previously run stage generated partitions for this shuffle, so for each output
      // that's still available, copy information about that output location to the new stage
      // (so we don't unnecessarily re-compute that data).
      val serLocs = mapOutputTracker.getSerializedMapOutputStatuses(shuffleDep.shuffleId)
      val locs = MapOutputTracker.deserializeMapStatuses(serLocs)
      (0 until locs.length).foreach { i =>
        if (locs(i) ne null) {
          // locs(i) will be null if missing
          stage.addOutputLoc(i, locs(i))
        }
      }
    } else {
      // Kind of ugly: need to register RDDs with the cache and map output tracker here
      // since we can't do it in the RDD constructor because # of partitions is unknown
      logInfo("Registering RDD " + rdd.id + " (" + rdd.getCreationSite + ")")
      mapOutputTracker.registerShuffle(shuffleDep.shuffleId, rdd.partitions.length)
    }
    stage
  }