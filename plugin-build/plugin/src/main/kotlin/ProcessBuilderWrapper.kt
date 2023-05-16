package rs.houtbecke.gradle.recorder.plugin

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.takeWhile
import kotlin.concurrent.thread
import kotlin.streams.toList as toKotlinList

abstract class ProcessBuilderWrapper(internal open val command: List<String>) {

    protected val outBuffer = MutableSharedFlow<String?>(replay = 10000, extraBufferCapacity = 0, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    protected val errBuffer = MutableSharedFlow<String?>(replay = 10000, extraBufferCapacity = 0, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val out: Flow<String> = outBuffer.takeWhile { it != null }.filterNotNull()
    val err: Flow<String> = errBuffer.takeWhile { it != null }.filterNotNull()

    open var shutdownHook: () -> Unit = {}
    abstract fun killProcessFamily()
}

class RealProcessBuilderWrapper(command:List<String>): ProcessBuilderWrapper(command) {
    private val processBuilder: ProcessBuilder
    private val process:Process
    private val shutdownHookThread: Thread = thread(start = false) {
        println("📹: Runtime shutting down")
        shutdownHook()
    }

    private val streamScope = CoroutineScope(Dispatchers.IO + CoroutineName("stdOut/stdErr reader"))

    init {
        println("📹: process command $command")
        processBuilder = ProcessBuilder(command)
        Runtime.getRuntime().addShutdownHook(shutdownHookThread)
        process = processBuilder.start()

        streamScope.launch {
            val reader = process.inputStream.bufferedReader()
            while(true) {
                val out: String? = reader.readLine()
                println("out: $out")
                outBuffer.emit(out)
                if (out == null) break
            }
        }

        streamScope.launch {
            val reader = process.errorStream.bufferedReader()
            while(true) {
                val err: String? = reader.readLine()
                println("err: $err")
                errBuffer.emit(err)
                if (err == null)
                    break
            }
        }
    }

    override fun killProcessFamily() {
        val processHandle = process.toHandle() ?: return
        (processHandle.descendants().toKotlinList() + processHandle).forEach {
            println("📹: destroy process ${it.info().commandLine()}")
            it.destroy()
        }
        Thread.sleep(3000)
        println("📹: main process isAlive: ${processHandle.isAlive}")
        streamScope.cancel("process being monitored has been killed")
        Runtime.getRuntime().removeShutdownHook(shutdownHookThread)
    }
}
