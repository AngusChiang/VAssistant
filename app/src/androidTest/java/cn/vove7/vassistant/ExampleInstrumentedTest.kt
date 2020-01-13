package cn.vove7.vassistant

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun queryMediaReceiver() { // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)

        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        println("====================================")
        appContext.packageManager.queryBroadcastReceivers(intent, 0).forEach {
            println(it)

        }
        println("====================================")
    }
}