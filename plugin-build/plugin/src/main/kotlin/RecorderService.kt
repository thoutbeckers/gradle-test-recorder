package rs.houtbecke.gradle.recorder.plugin

import kotlinx.coroutines.*
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.io.File

class AndroidRecording: Recording() {
    override fun command(video: File): List<String> =
        listOf(
            "/bin/sh",
            "-c",
            "adb shell \"while true; do screenrecord --output-format=h264 -; done\" | ffmpeg -y -i - ${video.absolutePath}"
        )
//      """
//      adb shell "while true; do screenrecord --output-format=h264 -; done" | ffmpeg -y -i - "${video.absolutePath}"
//      """.trimIndent()

}

abstract class Recording {

    private var state:State = State.Uninitialized

    sealed class State {
        object Uninitialized:State()
        open class Started(val processBuilderWrapper: ProcessBuilderWrapper, val scope:CoroutineScope = CoroutineScope(Dispatchers.Default)):State()
        class Recording(started:Started):Started(started.processBuilderWrapper, started.scope)
        object Stopped:State()
    }

    abstract fun command(video:File):List<String>

    internal fun start(video:File, processBuilderWrapperCreator: (command:List<String>) -> ProcessBuilderWrapper) {
        if (state != State.Uninitialized) {
            println("📹: not starting emulator recording, state is $state")
            return
        }

        val outputPath = video.absolutePath
        println("📹: Recording emulator output to $outputPath")

        val processBuilderWrapper = processBuilderWrapperCreator(command(video))
        val started = State.Started(processBuilderWrapper)
        state = started

        started.scope.launch {
            processBuilderWrapper.out.collect {
                println("📹 out: $it")
            }
        }

        started.scope.launch {
            processBuilderWrapper.err.collect {
                println("📹 err: $it")
            }
        }
    }

    internal fun stop() {
        val currentState = state
        if (currentState is State.Started) {
            state = State.Stopped
            currentState.processBuilderWrapper.killProcessFamily()
            currentState.scope.cancel()
            state = State.Uninitialized
        }
    }
}

abstract class AndroidRecorderService:RecorderService(
    recording = AndroidRecording(),
    processBuilderWrapper = { command: List<String> -> RealProcessBuilderWrapper(command) }
)

abstract class RecorderService(private val recording: Recording, private val processBuilderWrapper: (command:List<String>) -> ProcessBuilderWrapper) : BuildService<BuildServiceParameters.None>, AutoCloseable {

    fun start(video:File) {
        recording.start(video, processBuilderWrapper)
    }

    fun stop() {
        recording.stop()
    }

    override fun close() {
        println("📹: Build closing")
        recording.stop()
    }

}
