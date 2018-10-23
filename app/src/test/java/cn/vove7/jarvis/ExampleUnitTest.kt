package cn.vove7.jarvis

import cn.vove7.common.datamanager.parse.model.Action
import org.junit.Test
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.thread

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        val getParamsReg = "[\\S]*(\\([\\S]*\\))".toRegex()
        arrayOf(
                "setTextById(消息,input)",
                "open(QQ)",
                "sleep"
        ).forEach {

            val mResult = getParamsReg.matchEntire(it)
            var ps: List<String?>
            val c =
                if (mResult != null) {
                    val param = mResult.groupValues[1]
                    ps = param.substring(1, param.length - 1).split(",")
                    it.substring(0, mResult.groups[1]?.range?.first ?: it.length)
                } else {
                    ps = listOf(null)
                    it
                }
            println("$c $ps")

        }

    }

    @Test
    fun simpleTest() {
        println("123".startsWith(""))
    }

    @Test
    fun regTest() {
        //匹配包名 至少一个.
        val r = "[a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)+".toRegex()
        mapOf(
                Pair("cn.vove7.ok", true),
                Pair("Alipay", false)
        ).forEach {
            println(r.matches(it.key) == it.value)
        }
    }

    @Test
    fun waitTest() {
        val millis = 2000L
        val lock = Object()
        val t = thread {
            val begin = System.currentTimeMillis()
            if (millis < 0) {
                lock.wait()
                println("执行器-解锁")
            } else {
                var end: Long = 0
                synchronized(lock) {
                    lock.wait(millis)
                    end = System.currentTimeMillis()
                    println("$begin -- $end")
                }
                println("执行器-解锁")

                if (end - begin >= millis) {//自动超时 终止执行
                    println("自动解锁")
                }
            }
        }
        while (t.isAlive) sleep(1000)
    }

    @Test
    fun testActionQ() {
        val q = PriorityQueue<Action>()
        q.add(Action("123", ""))
        q.add(Action("456", ""))
        q.add(Action("789", ""))
        q.add(Action(2, "0", ""))
        q.add(Action(-1, "-1", ""))
        while (q.isNotEmpty()) {
            println(q.poll())
        }
    }


    @Test
    fun testArrayIn() {
        arrayOf("媒体音量", "铃声音量", "通知音量").let {
            it.forEach { s ->
                println(it.indexOf(s) in 0..2)
            }
            println(it.indexOf("1") in 0..2)

        }
    }

    @Test
    fun testParseDate() {
        arrayOf(
                "十二点", "八点四十五", "八点半",
                "明天中午", "后天下午", "晚上八点"  //is pm true

        ).forEach {
            //parse
            System.currentTimeMillis()
        }


    }
}