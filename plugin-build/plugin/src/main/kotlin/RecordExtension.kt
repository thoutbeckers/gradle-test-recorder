package rs.houtbecke.gradle.recorder.plugin

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

const val DEFAULT_VIDEO_OUTPUT_FILE = "video.mp4"

@Suppress("UnnecessaryAbstractClass")
abstract class RecordExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    val videoOutput: RegularFileProperty = objects.fileProperty().convention(
        project.layout.buildDirectory.file(DEFAULT_VIDEO_OUTPUT_FILE)
    )
}
