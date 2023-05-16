package rs.houtbecke.gradle.recorder.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

const val RECORD_EXTENSION_NAME = "recordConfig"

const val START_RECORD_ANDROID_TASK_NAME = "recordAndroid"
const val STOP_RECORD_ANDROID_TASK_NAME = "stopRecordAndroid"

abstract class RecorderPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val recordExtension = project.extensions.create(RECORD_EXTENSION_NAME, RecordExtension::class.java, project)

        project.gradle.sharedServices.registerIfAbsent("AndroidRecorderService", AndroidRecorderService::class.java) {}

        project.tasks.register(START_RECORD_ANDROID_TASK_NAME, StartRecordTask::class.java) { task ->
            task.videoOutput.set(recordExtension.videoOutput)
        }

        project.tasks.register(STOP_RECORD_ANDROID_TASK_NAME, StopRecordTask::class.java) { _ ->
        }

    }
}
