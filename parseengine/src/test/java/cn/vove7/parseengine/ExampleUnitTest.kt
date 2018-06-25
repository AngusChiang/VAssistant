package cn.vove7.parseengine

import cn.vove7.datamanager.parse.model.Action
import cn.vove7.parseengine.engine.ParseEngine
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())
    }

    /**
     * 测试
     */
    @Test
    fun parseTest() {
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
            val s = ParseEngine.parseAction(it.key)
            assertEquals(s.isSuccess, it.value)
            clearUp(s.actionQueue)
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
    }

}