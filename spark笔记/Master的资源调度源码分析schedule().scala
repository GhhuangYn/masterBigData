/**
     为等待中的apps、Driver、Executor 调度可用的资源。
     每次有新的app加入或者可用资源改变都会触发该方法
   */
  private def schedule(): Unit = {

    //如果master的状态是不是alive的话，直接返回
    if (state != RecoveryState.ALIVE) {
      return
    }
    // Drivers 具有严格的优先权，首先进行调度

    /*
      Random.shuffle()：就是对传入的集合元素进行打乱
      取出之前注册的worker,状态必须为alive,然后进行随机shuffle
    */
    val shuffledAliveWorkers = Random.shuffle(workers.toSeq.filter(_.state == WorkerState.ALIVE))
    val numWorkersAlive = shuffledAliveWorkers.size   
    var curPos = 0

    /*
      首先调度Driver。
      为什么要调度Driver?什么情况下会注册Driver，并导致Driver被调度?
      其实，只有在yarn-cluster模式提交的时候才会注册Driver，
      因为其他的两种模式都是在Clent本地启动Driver的，而不必向Master注册Driver(仅注册App)，当然也不让Master调度Driver了
      Master的注册在spark-sumbit就会通过Client类发生
    */
    for (driver <- waitingDrivers.toList) { // 遍历所有正在等待的队列
      // We assign workers to each waiting driver in a round-robin fashion. For each driver, we
      // start from the last worker that was assigned a driver, and continue onwards until we have
      // explored all alive workers.
      var launched = false
      var numWorkersVisited = 0

      // 只要有alive的worker,而且当前的Driver还没有被启动，就继续遍历
      while (numWorkersVisited < numWorkersAlive && !launched) { 
        val worker = shuffledAliveWorkers(curPos)	// 取出一个worker
        numWorkersVisited += 1

        //判断当前worker的资源情况
        if (worker.memoryFree >= driver.desc.mem && worker.coresFree >= driver.desc.cores) {
          launchDriver(worker, driver)  // 在当前的worker中启动Driver
          waitingDrivers -= driver      // 从等待队列中移除该driver
          launched = true               // 到此该driver启动成功
        }
        curPos = (curPos + 1) % numWorkersAlive   // 将指针指向下一个worker
      }
    }   //到此，所有的Driver注册完毕
    // 当启动Driver启动之后,调度Application
    startExecutorsOnWorkers()     
  }

  /*
      启动Driver
  */
  private def launchDriver(worker: WorkerInfo, driver: DriverInfo) {
    logInfo("Launching driver " + driver.id + " on worker " + worker.id)
    worker.addDriver(driver)      //将Driver加入worker内存的缓存结构
    driver.worker = Some(worker)  //同时在Driver中加入worker信息
    //向worker发送LaunchDriver消息，让work启动Driver
    worker.endpoint.send(LaunchDriver(driver.id, driver.desc))
    driver.state = DriverState.RUNNING
  }

  def addDriver(driver: DriverInfo) {
    drivers(driver.id) = driver
    memoryUsed += driver.desc.mem   // 调整内存
    coresUsed += driver.desc.cores  // 调整cpu core
  }

   /**
   * 在worker上调度资源和启动executors
   	 资源的调度和core分配是由master统一进行的，
   */
   private def startExecutorsOnWorkers(): Unit = {
    // Right now this is a very simple FIFO scheduler. We keep trying to fit in the first app
    // in the queue, then the second app, etc.

    // 遍历等待队列中的application
    for (app <- waitingApps if app.coresLeft > 0) {
      // 每个executor所需的core
      val coresPerExecutor: Option[Int] = app.desc.coresPerExecutor
      // 过滤掉不够资源启动executor的worker，按照剩余core数量倒序排序
      val usableWorkers = workers.toArray.filter(_.state == WorkerState.ALIVE)
        .filter(worker => worker.memoryFree >= app.desc.memoryPerExecutorMB &&
          worker.coresFree >= coresPerExecutor.getOrElse(1))
        .sortBy(_.coresFree).reverse

      //获取每个worker分配的core数量
      val assignedCores = scheduleExecutorsOnWorkers(app, usableWorkers, spreadOutApps)

      // 我们决定了每个worker分配多少core，然后开始分配
      // 遍历每个可用的worker, 且需要分配的core数量大于0
      for (pos <- 0 until usableWorkers.length if assignedCores(pos) > 0) {
        // 分配方法
        allocateWorkerResourceToExecutors(
          app, assignedCores(pos), coresPerExecutor, usableWorkers(pos))
      }
    }
  }

  /* Schedule executors to be launched on the workers.
  * 返回一个数组：包含每个worker分配到的cores数量
  */
  private def scheduleExecutorsOnWorkers(
    app: ApplicationInfo,
    usableWorkers: Array[WorkerInfo],
    spreadOutApps: Boolean): Array[Int] = {
  val coresPerExecutor = app.desc.coresPerExecutor          // 每个executor所需的core(--executor-cores参数)
  val minCoresPerExecutor = coresPerExecutor.getOrElse(1)   // 如果提交时设定了--executor-cores参数，那么会读取，否则设置为1
  val oneExecutorPerWorker = coresPerExecutor.isEmpty       // 每个worker是否只有一个executor
  val memoryPerExecutor = app.desc.memoryPerExecutorMB      // 每个executor所需的内存(--executor-memory MEM参数)
  val numUsable = usableWorkers.length                      // 可用worker的数量
  val assignedCores = new Array[Int](numUsable)             // 存放每个worker分配到的core数量
  val assignedExecutors = new Array[Int](numUsable)         // 存放每个worker启动的executor数量
  // app.coresLeft是app剩余需要分配的core,与可用worker的core总数之间取小者，得出最后需要分配出去的core数量
  var coresToAssign = math.min(app.coresLeft, usableWorkers.map(_.coresFree).sum)   

  /** 判断某个worker是否可以为该app启动一个executor, 其实就是检查资源情况*/
  def canLaunchExecutor(pos: Int): Boolean = {
    // 要分配的core数量是否大于每个executor最少core
    val keepScheduling = coresToAssign >= minCoresPerExecutor  
    // 当前的worker是否有足够的core
    val enoughCores = usableWorkers(pos).coresFree - assignedCores(pos) >= minCoresPerExecutor

    // 如果我们允许一个worker上有多个executor, 那么我们可以总是启动一个新的executor
    // 否则, 如果worker已经有一个executor, 那么只要往executor加core
    val launchingNewExecutor = !oneExecutorPerWorker || assignedExecutors(pos) == 0 // 判断标记

    //如果允许每次启动新的Executor
    if (launchingNewExecutor) {   
      // 每个worker所需总的memory
      val assignedMemory = assignedExecutors(pos) * memoryPerExecutor  
      // 判断内存是否足够(可用内存-所需分配内存>=一个executor所占的内存,意思是还可以至少分配一个exe)
      val enoughMemory = usableWorkers(pos).memoryFree - assignedMemory >= memoryPerExecutor
      // 判断当前的所有的executor数量与app当前的executor数量之和是否超过最大限制
      val underLimit = assignedExecutors.sum + app.executors.size < app.executorLimit
      // 综合上述条件, 返回最终的结果
      keepScheduling && enoughCores && enoughMemory && underLimit
    } else {
      // 往已经存在的exector中添加core, 因此不必检查内存和executor限制
      keepScheduling && enoughCores
    }
  }

  // 不断启动executor直到没有更多的worker适配任何的executor，或者达到了app的限制
  // 除了yarn-only模式下可以指定num executor
  // 在standalone模式下, 使用--total executor cores和--executor cores确定executor数量

  // 过滤出能够工作的worker的索引
  var freeWorkers = (0 until numUsable).filter(canLaunchExecutor)  

  // 只要还有满足条件的worker，就继续遍历，直到core分配完毕
  while (freeWorkers.nonEmpty) {

    //遍历每个worker
    freeWorkers.foreach { pos =>
      var keepScheduling = true
      // 如果当前的这个work资源满足工作
      while (keepScheduling && canLaunchExecutor(pos)) {

        // 计算分配资源
        coresToAssign -= minCoresPerExecutor          //需要分配core数量-一个executor的core数量
        assignedCores(pos) += minCoresPerExecutor     //分配到的core递增

        // 如果我们在一个worker上只启动一个executor，那么每次遍历分配1个core给executor
        // 否则，每次创建一个新的executor，并且把一个core分配给它

        // 在这里其实并没有去计算每个worker需要多少个executor
        // 而是每次迭代就增加executor，或者只分配core，直到core分配完毕，executor也分配完毕
        if (oneExecutorPerWorker) {
          assignedExecutors(pos) = 1
        } else {
          assignedExecutors(pos) += 1
        }

        // Application的调度算法有两种，一种是spreadOutApps，另一种是非spreadOutApps
        // spreadOutApps算法意味着把executors分配给尽可能多的worker
        // 如果不用这种模式,我们应该持续在一个worker上调度executor直到这个worker的资源使用完毕。
        // 然后才移动到下一个worker，即尽量少的使用worker。默认情况下使用spreadOut模式
        if (spreadOutApps) {
          //如果是spreadOut模式，在分配了一个executor之后，就结束循环，进入下一个worker，
          //否则就继续分配，直到这个worker的资源用尽
          keepScheduling = false   
        }
      } 
      // 这里while循环结束，如果使用的是spreadOutApp,那么该worker的第一次分配结束
      // 它可能分配到1个executor和若干个core
    }  
     //这里一次workers的遍历就结束了，如果使用的是spreadOutApp分配算法
    	//那么到了这一步，executor被平均分配到尽可能多的worker中

    // 过滤掉不可用的workers，然后进入下一次循环当中，如果还有core没有被分配，那么就进入下一次循环
    freeWorkers = freeWorkers.filter(canLaunchExecutor)
  }

  // 最后返回每个worker分配的core数量
  assignedCores
  }

  /**
   * 为一个或多个executor分配work上的资源(core)
   * @param app the info of the application which the executors belong to
   * @param 该worker上需要分配的core数量
   * @param 每个executor的core数量
   * @param worker the worker info
  */
  private def allocateWorkerResourceToExecutors(
      app: ApplicationInfo,
      assignedCores: Int,
      coresPerExecutor: Option[Int],
      worker: WorkerInfo): Unit = {
    // 如果每个executor的core数量是具体的，我们把需要分配到这个worker的core平均分配到executors
    // 如果每个executor的core数量不明确，启动一个单独的executor，把所有的core都分配给它

    // 求出每个worker的executor的数量( 每个workrer的core/每个executor的core)  默认为1 
    val numExecutors = coresPerExecutor.map { assignedCores / _ }.getOrElse(1)
    // 获取每个executor的需要分配core的数量，默认为该work分配的所有cores
    val coresToAssign = coresPerExecutor.getOrElse(assignedCores)

    // 开始启动numExecutor个executor,每个exe包含coresToAssign个core
    for (i <- 1 to numExecutors) {

      //添加executor到app，返回一个executor，并且在worker启动该executor
      // executor包含信息： 在哪个worker分配多少个core
      val exec = app.addExecutor(worker, coresToAssign)
      launchExecutor(worker, exec)
      app.state = ApplicationState.RUNNING
    }
  }

  /*
    启动executor
  */
  private def launchExecutor(worker: WorkerInfo, exec: ExecutorDesc): Unit = {
    logInfo("Launching executor " + exec.fullId + " on worker " + worker.id)
    worker.addExecutor(exec)
    // 向worker发送启动executor消息（使用LaunchExecutor封装）
    worker.endpoint.send(LaunchExecutor(masterUrl,
      exec.application.id, exec.id, exec.application.desc, exec.cores, exec.memory))
    // 向Driver发送添加了executor消息（使用ExecutorAdded封装）
    exec.application.driver.send(
      ExecutorAdded(exec.id, worker.id, worker.hostPort, exec.cores, exec.memory))
  }

  //ApplicationInfo的addExecutor()方法
  private[master] def addExecutor(
      worker: WorkerInfo,
      cores: Int,
      useID: Option[Int] = None): ExecutorDesc = {
    val exec = new ExecutorDesc(newExecutorId(useID), this, worker, cores, desc.memoryPerExecutorMB)
    executors(exec.id) = exec
    coresGranted += cores
    exec
  }

  //workerInfo的addExecutor()方法
  def addExecutor(exec: ExecutorDesc) {
    executors(exec.fullId) = exec
    coresUsed += exec.cores
    memoryUsed += exec.memory
  }


