package rs.houtbecke.gradle.recorder.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option

abstract class StartRecordTask : DefaultTask() {

    init {
        description = "Record video of the Android Emulator"
        group = BasePlugin.BUILD_GROUP
        outputs.upToDateWhen { false }
    }

    @get:ServiceReference("AndroidRecorderService")
    abstract val service: Property<AndroidRecorderService>

    @get:OutputFile
    //@get:Option(option = "file", description = "filename of the output file")
    abstract val videoOutput: RegularFileProperty

    @TaskAction
    fun startRecordingAction() {
        service.get().start(videoOutput.asFile.get())
    }
}
abstract class StopRecordTask : DefaultTask() {
    init {
        description = "Stop recording video of the Android emulator"
        group = BasePlugin.BUILD_GROUP
    }

    @get:ServiceReference("AndroidRecorderService")
    abstract val service: Property<AndroidRecorderService>

    @TaskAction
    fun stopRecordingAction() {
        service.get().stop()
    }
}
