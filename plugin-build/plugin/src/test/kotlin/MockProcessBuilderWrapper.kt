package rs.houtbecke.gradle.recorder.plugin

class MockProcessBuilderWrapper:ProcessBuilderWrapper(listOf("")) {

    var isShutdown = false
    override var shutdownHook = {
        isShutdown = true
    }

    override var command:List<String> = listOf("")

    fun mockStdErr(errOutput:String) = errBuffer.tryEmit(errOutput)
    fun mockStdOut(outOutput:String) = outBuffer.tryEmit(outOutput)

    var isKilled = false

    override fun killProcessFamily() {
        isKilled = true
    }

}
