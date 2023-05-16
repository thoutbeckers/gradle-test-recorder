package rs.houtbecke.gradle.recorder.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class RecorderPluginTest {

    @Test
    fun `plugin is applied correctly to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("rs.houtbecke.gradle.recorder.plugin")

        assert(project.tasks.getByName("recordAndroid") is StartRecordTask)
    }

    @Test
    fun `extension recordConfig is created correctly`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("rs.houtbecke.gradle.recorder.plugin")

        assertNotNull(project.extensions.getByName("recordConfig"))
    }

    @Test
    fun `parameters are passed correctly from extension to task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("rs.houtbecke.gradle.recorder.plugin")
        val aFile = File(project.projectDir, "testVideo.mp4")
        (project.extensions.getByName("recordConfig") as RecordExtension).apply {
            videoOutput.set(aFile)
        }

        val recordAndroidEmulator = project.tasks.getByName("recordAndroid") as StartRecordTask

        val path = recordAndroidEmulator.videoOutput.asFile.get().absolutePath
        assertTrue(path.endsWith("/testVideo.mp4"))
        assertEquals(aFile, recordAndroidEmulator.videoOutput.get().asFile)
    }
}
