package cn.vove7.parseengine

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import cn.vove7.parseengine.utils.app.AppHelper
import org.junit.Assert.assertEquals
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
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()

        assertEquals("cn.vove7.parseengine.test", appContext.packageName)
    }

    fun getContext(): Context {
        return InstrumentationRegistry.getTargetContext()
    }

    @Test
    fun matchAppName() {
        val names = arrayOf(
                "Q",
                "QQ",
                "微博",
                "音乐"
        )
        val appHelper = AppHelper(getContext())
        names.forEach {
            println(appHelper.matchAppName(it))
        }
    }

}
