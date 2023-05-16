package rs.houtbecke.gradle.recorder.plugin

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class RealProcessBuilderWrapperTest {


    @Test
    @Ignore
    fun testRealProcessBuilderWrapperComplex():Unit = runBlocking {
        RealProcessBuilderWrapper(listOf("/bin/sh", "-c", "adb shell \"while true; do screenrecord --output-format=h264 -; done\" | ffmpeg -y -i - ~/temp/bla.mp4"))
    }


    @Test
    fun testRealProcessBuilderWrapper() = runBlocking {
        val rpw = RealProcessBuilderWrapper(listOf("adb", "version"))

        val out = rpw.out.toList()
        assertTrue(out.size > 1)
        assertTrue(out[0].startsWith("Android Debug Bridge"))
    }

    @Test
    fun testRealProcessBuilderWrapperError() = runBlocking {
        val rpw = RealProcessBuilderWrapper(listOf("adb", "derp"))
        val err = rpw.err.toList()
        assertTrue(err.isNotEmpty())
        assertEquals("adb: unknown command derp", err[0])
    }

}
