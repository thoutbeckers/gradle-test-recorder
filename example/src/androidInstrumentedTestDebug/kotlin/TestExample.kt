import androidx.test.core.app.launchActivity
import org.junit.Test
import rs.houtbecke.example.MainActivity

class TestExample {

    @Test
    fun runActivity():Unit =
        launchActivity<MainActivity>().use {
            Thread.sleep(5000)
        }
}
