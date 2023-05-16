package rs.houtbecke.gradle.recorder.plugin

import org.gradle.api.services.BuildServiceParameters
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
class AndroidRecorderServiceTest:RecorderServiceTest() {

    private val recording = AndroidRecording()
    private val service = MockRecordingService(recording)

    @Test
    fun `can start and stop AndroidRecording`() {
        service.start(File("test.mp4"))
        val firstPart = "/bin/sh -c adb shell \"while true; do screenrecord --output-format=h264 -; done\" | ffmpeg -y -i -"
        val lastPart = "/plugin-build/plugin/test.mp4"
        assertTrue(mockProcessBuilderWrapper.command.joinToString(" ").startsWith(firstPart))
        assertTrue(mockProcessBuilderWrapper.command.joinToString(" ").endsWith(lastPart))

        mockProcessBuilderWrapper.apply {

            assertFalse(isKilled)
            service.stop()
            assertTrue(isKilled)

            assertFalse(isShutdown)
            mockProcessBuilderWrapper.shutdownHook() // is normally only called via the Runtime closing
            assertTrue(isShutdown)
        }
    }

}

open class RecorderServiceTest {
    protected val mockProcessBuilderWrapper = MockProcessBuilderWrapper()
    private val project = ProjectBuilder.builder().build()

    @Before
    fun setUpProject() {
        project.pluginManager.apply("rs.houtbecke.gradle.recorder.plugin")
    }
    inner class MockRecordingService(recording: Recording): RecorderService(
        recording = recording,
        processBuilderWrapper = { mockProcessBuilderWrapper.command = it; mockProcessBuilderWrapper}) {
        override fun getParameters(): BuildServiceParameters.None {
            error("no parameters")
        }
    }
}
