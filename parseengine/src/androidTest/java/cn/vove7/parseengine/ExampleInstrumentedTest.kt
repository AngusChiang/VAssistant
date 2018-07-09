package cn.vove7.parseengine

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import cn.vove7.datamanager.DAO
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.parseengine.engine.ParseEngine
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

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

    /**
     * 测试
     */
    @Test
    fun parseTest() {
        DAO.init(getContext())
        val testDatas = mapOf(
//                Pair("打电话给abc", true),
//                Pair("打给abc", true),
//                Pair("给abc打电话", true),
//                Pair("打开QQ", true),
                Pair("打开QQ给abc发消息", true),
                Pair("打开QQ发消息给abc", true),
                Pair("打开QQ发消息给abc内容哦", true),
                Pair("打开QQ发消息给abc内容为好啊啊", true),
                Pair("打QQ开", false),
                Pair("打开微博", true)
        )
        testDatas.forEach {
            val s = ParseEngine.parseGlobalAction(it.key, "")
            clearUp(s.actionQueue)
            val q = ParseEngine.matchAppAction("", "com.tentcnt.mobileqq")
            clearUp(q)

        }

    }

    private fun clearUp(actions: PriorityQueue<Action>) {
        var p: Action
        var index = 0
        while (actions.isNotEmpty()) {
            p = actions.poll()
            println("Step-${index++}: ${p.matchWord} - ${p.param} ")
        }
        println()
        println()
        println()
    }
}
