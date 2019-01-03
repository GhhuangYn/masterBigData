/*
    worker接收到master发送过来的启动Driver的消息
    在worker中启动Driver
*/

case LaunchDriver(driverId, driverDesc) =>
      logInfo(s"Asked to launch driver $driverId")
      //构建一个DriverRunner,用于管理一个driver的执行
      val driver = new DriverRunner(
        conf,
        driverId,
        workDir,
        sparkHome,
        driverDesc.copy(command = Worker.maybeUpdateSSLSettings(driverDesc.command, conf)),
        self,
        workerUri,
        securityMgr)
      drivers(driverId) = driver
      //开始启动一个Driver
      driver.start()    

      // 修改本worker的资源数量
      coresUsed += driverDesc.cores
      memoryUsed += driverDesc.mem

/*
  DriverRunner
*/

 /** Starts a thread to run and manage the driver. */
  private[worker] def start() = {

    //启动一个Java线程
    new Thread("DriverRunner for " + driverId) {
      override def run() {
        var shutdownHook: AnyRef = null
        try {
          shutdownHook = ShutdownHookManager.addShutdownHook { () =>
            logInfo(s"Worker shutting down, killing driver $driverId")
            kill()
          }

          // 准备driver的jars然后启动Driver
          // 此方法执行完毕之后，Driver启动完毕，或出现异常
          val exitCode = prepareAndRunDriver()


          // set final state depending on if forcibly killed and process exit code
          finalState = if (exitCode == 0) {
            Some(DriverState.FINISHED)
          } else if (killed) {
            Some(DriverState.KILLED)
          } else {
            Some(DriverState.FAILED)
          }
        } catch {
          case e: Exception =>
            kill()
            finalState = Some(DriverState.ERROR)
            finalException = Some(e)
        } finally {
          if (shutdownHook != null) {
            ShutdownHookManager.removeShutdownHook(shutdownHook)
          }
        }

        // 向worker发送driver最后的状态，可能是异常
        worker.send(DriverStateChanged(driverId, finalState.get, finalException))
      }
    }.start()
  }

  private[worker] def prepareAndRunDriver(): Int = {
    // 创建Driver的工作目录
    val driverDir = createWorkingDirectory()
    // 下载jar包
    val localJarFilename = downloadUserJar(driverDir)

    def substituteVariables(argument: String): String = argument match {
      case "{{WORKER_URL}}" => workerUrl
      case "{{USER_JAR}}" => localJarFilename
      case other => other
    }

    // processBuilder是一个进程启动的工具
    // 使用ProcessBuilder封装启动Driver的相关信息和启动命令
    val builder = CommandUtils.buildProcessBuilder(driverDesc.command, securityManager,
      driverDesc.mem, sparkHome.getAbsolutePath, substituteVariables)

    runDriver(builder, driverDir, driverDesc.supervise)
  }

  // 启动Driver
  private def runDriver(builder: ProcessBuilder, baseDir: File, supervise: Boolean): Int = {
    builder.directory(baseDir)

    //初始化一些输出信息并且格式化命令
    def initialize(process: Process): Unit = {
      // 重定向输出目录
      val stdout = new File(baseDir, "stdout")
      CommandUtils.redirectStream(process.getInputStream, stdout)

      val stderr = new File(baseDir, "stderr")
      val formattedCommand = builder.command.asScala.mkString("\"", "\" \"", "\"")
      val header = "Launch Command: %s\n%s\n\n".format(formattedCommand, "=" * 40)
      Files.append(header, stderr, StandardCharsets.UTF_8)
      CommandUtils.redirectStream(process.getErrorStream, stderr)
    }

    // 真正启动Driver的方法
    runCommandWithRetry(ProcessBuilderLike(builder), initialize, supervise)
  }

  private[worker] def runCommandWithRetry(
      command: ProcessBuilderLike, initialize: Process => Unit, supervise: Boolean): Int = {
    var exitCode = -1
    // Time to wait between submission retries.
    var waitSeconds = 1
    // A run of this many seconds resets the exponential back-off.
    val successfulRunDuration = 5
    var keepTrying = !killed

    while (keepTrying) {
      logInfo("Launch Command: " + command.command.mkString("\"", "\" \"", "\""))

      synchronized {
        if (killed) { return exitCode }
        process = Some(command.start())
        initialize(process.get)
      }

      val processStart = clock.getTimeMillis()

      // 开始启动Drvier进程，并等待启动的结果
      exitCode = process.get.waitFor()

      // 如果有必要, 需要重试, 进入下一个循环
      keepTrying = supervise && exitCode != 0 && !killed
      if (keepTrying) {
        if (clock.getTimeMillis() - processStart > successfulRunDuration * 1000) {
          waitSeconds = 1
        }
        logInfo(s"Command exited with status $exitCode, re-launching after $waitSeconds s.")
        sleeper.sleep(waitSeconds)
        waitSeconds = waitSeconds * 2 // exponential back-off
      }
    }

    exitCode
  }

  // DriverRunner End


/*
    在worker中启动Executor
*/

 case LaunchExecutor(masterUrl, appId, execId, appDesc, cores_, memory_) =>
      if (masterUrl != activeMasterUrl) {
        logWarning("Invalid Master (" + masterUrl + ") attempted to launch executor.")
      } else {
        try {
          logInfo("Asked to launch executor %s/%d for %s".format(appId, execId, appDesc.name))

          // 创建executor的工作目录
          val executorDir = new File(workDir, appId + "/" + execId)
          if (!executorDir.mkdirs()) {
            throw new IOException("Failed to create directory " + executorDir)
          }

          // 为executor创建本地的工作目录，通过SPARK_EXECUTOR_DIRS环境变量传给executor
          // 当application结束时worker会检测到
          val appLocalDirs = appDirectories.getOrElse(appId, {
            val localRootDirs = Utils.getOrCreateLocalRootDirs(conf)
            val dirs = localRootDirs.flatMap { dir =>
              try {
                val appDir = Utils.createDirectory(dir, namePrefix = "executor")
                Utils.chmod700(appDir)
                Some(appDir.getAbsolutePath())
              } catch {
                case e: IOException =>
                  logWarning(s"${e.getMessage}. Ignoring this directory.")
                  None
              }
            }.toSeq
            if (dirs.isEmpty) {
              throw new IOException("No subfolder can be created in " +
                s"${localRootDirs.mkString(",")}.")
            }
            dirs
          })
          appDirectories(appId) = appLocalDirs
          // 创建一个ExecutorRunner
          val manager = new ExecutorRunner(
            appId,
            execId,
            appDesc.copy(command = Worker.maybeUpdateSSLSettings(appDesc.command, conf)),
            cores_,
            memory_,
            self,
            workerId,
            host,
            webUi.boundPort,
            publicAddress,
            sparkHome,
            executorDir,
            workerUri,
            conf,
            appLocalDirs, ExecutorState.RUNNING)
          executors(appId + "/" + execId) = manager
          // 启动executor进程
          manager.start()
          // executor启动完毕
          coresUsed += cores_
          memoryUsed += memory_
          // 向master发送executor最后的状态
          sendToMaster(ExecutorStateChanged(appId, execId, manager.state, None, None))
        } catch {
          case e: Exception =>
            logError(s"Failed to launch executor $appId/$execId for ${appDesc.name}.", e)
            if (executors.contains(appId + "/" + execId)) {
              executors(appId + "/" + execId).kill()
              executors -= appId + "/" + execId
            }
            sendToMaster(ExecutorStateChanged(appId, execId, ExecutorState.FAILED,
              Some(e.toString), None))
        }
      }

      //========ExecutorRunner===========
      private[worker] def start() {

        // 创建一个线程
        workerThread = new Thread("ExecutorRunner for " + fullId) {
          override def run() { fetchAndRunExecutor() }
        }

        //启动线程
        workerThread.start()
        // Shutdown hook that kills actors on shutdown.
        shutdownHook = ShutdownHookManager.addShutdownHook { () =>
          // It's possible that we arrive here before calling `fetchAndRunExecutor`, then `state` will
          // be `ExecutorState.RUNNING`. In this case, we should set `state` to `FAILED`.
          if (state == ExecutorState.RUNNING) {
            state = ExecutorState.FAILED
          }
          killProcess(Some("Worker shutting down")) }
      }

  /**
   * Download and run the executor described in our ApplicationDescription
   */
  private def fetchAndRunExecutor() {
    try {
      // 启动一个processBuilder, 用于封装启动命令并启动executor
      val builder = CommandUtils.buildProcessBuilder(appDesc.command, new SecurityManager(conf),
        memory, sparkHome.getAbsolutePath, substituteVariables)
      // 获取启动命令，并格式化
      val command = builder.command()
      val formattedCommand = command.asScala.mkString("\"", "\" \"", "\"")
      logInfo(s"Launch command: $formattedCommand")

      // 设置相关的环境变量
      builder.directory(executorDir)
      builder.environment.put("SPARK_EXECUTOR_DIRS", appLocalDirs.mkString(File.pathSeparator))
      builder.environment.put("SPARK_LAUNCH_WITH_SCALA", "0")

      // Add webUI log urls
      val baseUrl =
        if (conf.getBoolean("spark.ui.reverseProxy", false)) {
          s"/proxy/$workerId/logPage/?appId=$appId&executorId=$execId&logType="
        } else {
          s"http://$publicAddress:$webUiPort/logPage/?appId=$appId&executorId=$execId&logType="
        }
      builder.environment.put("SPARK_LOG_URL_STDERR", s"${baseUrl}stderr")
      builder.environment.put("SPARK_LOG_URL_STDOUT", s"${baseUrl}stdout")

      // 使用processBuilder的属性创建一个新的进程
      process = builder.start()
      val header = "Spark Executor Command: %s\n%s\n\n".format(
        formattedCommand, "=" * 40)

      // Redirect its stdout and stderr to files
      val stdout = new File(executorDir, "stdout")
      stdoutAppender = FileAppender(process.getInputStream, stdout, conf)

      val stderr = new File(executorDir, "stderr")
      Files.write(header, stderr, StandardCharsets.UTF_8)
      stderrAppender = FileAppender(process.getErrorStream, stderr, conf)

      // 启动executor进程，并结果代码，如果为0就是启动成功
      val exitCode = process.waitFor()
      state = ExecutorState.EXITED
      val message = "Command exited with code " + exitCode
      // 向worker发送executor最后的状态
      worker.send(ExecutorStateChanged(appId, execId, state, Some(message), Some(exitCode)))
    } catch {
      case interrupted: InterruptedException =>
        logInfo("Runner thread for executor " + fullId + " interrupted")
        state = ExecutorState.KILLED
        killProcess(None)
      case e: Exception =>
        logError("Error running executor", e)
        state = ExecutorState.FAILED
        killProcess(Some(e.toString))
    }
  }

  /*
    Master在receive()方法中接收ExecutorStateChanged消息
  */
  case ExecutorStateChanged(appId, execId, state, message, exitStatus) =>
      val execOption = idToApp.get(appId).flatMap(app => app.executors.get(execId))
      execOption match {
        case Some(exec) =>
          val appInfo = idToApp(appId)
          val oldState = exec.state
          exec.state = state

          if (state == ExecutorState.RUNNING) {
            assert(oldState == ExecutorState.LAUNCHING,
              s"executor $execId state transfer from $oldState to RUNNING is illegal")
            appInfo.resetRetryCount()
          }

          // 向Driver反向注册executor的信息
          exec.application.driver.send(ExecutorUpdated(execId, state, message, exitStatus, false))

          if (ExecutorState.isFinished(state)) {
            // Remove this executor from the worker and app
            logInfo(s"Removing executor ${exec.fullId} because it is $state")
            // If an application has already finished, preserve its
            // state to display its information properly on the UI
            if (!appInfo.isFinished) {
              appInfo.removeExecutor(exec)
            }
            exec.worker.removeExecutor(exec)

            val normalExit = exitStatus == Some(0)
            // Only retry certain number of times so we don't go into an infinite loop.
            // Important note: this code path is not exercised by tests, so be very careful when
            // changing this `if` condition.
            if (!normalExit
                && appInfo.incrementRetryCount() >= MAX_EXECUTOR_RETRIES
                && MAX_EXECUTOR_RETRIES >= 0) { // < 0 disables this application-killing path
              val execs = appInfo.executors.values
              if (!execs.exists(_.state == ExecutorState.RUNNING)) {
                logError(s"Application ${appInfo.desc.name} with ID ${appInfo.id} failed " +
                  s"${appInfo.retryCount} times; removing it")
                removeApplication(appInfo, ApplicationState.FAILED)
              }
            }
          }
          //重新进行资源调度
          schedule()