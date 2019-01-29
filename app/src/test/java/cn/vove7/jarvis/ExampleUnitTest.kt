package cn.vove7.jarvis

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Range
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.utils.TextDateParser
import cn.vove7.common.utils.TextHelper
import cn.vove7.common.utils.whileWaitCount
import cn.vove7.common.utils.whileWaitTime
import cn.vove7.jarvis.chat.TulingChatSystem
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import io.github.kbiakov.codeview.highlight.hex
import org.jsoup.Jsoup
import org.junit.Test
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch
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
        val s = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        arrayOf(
//                "中午", "十二点", "八点四十五", "八点半", "晚上八点", "中午12点", "下午2点一刻",
//                "明天中午", "后天下午3点", "大后天中午", "昨天下午2:21", "前天下午两点半",
//                "周一下午", "下周二八点半", "周日晚上八点",
//                "二十号晚上七点", "21号", "二十八号", "下个月十八号上午8点二十三", "十二月25号",
//                "12月8号上午8点", "周二一点", "这周五八点", "周五晚上7点半",
                "一小时后", "一个半小时后", "半小时后", "两个半小时后", "45分钟后", "三十二分钟后",
                "两个小时后", "两小时后", "二十小时后",
                "八天后", "你好",
                "明晚9点", "明天这个时候", "下个月2号这个时间",
                "1小时24分钟后"
        ).forEach {
            //parse
            val ss = TextDateParser.parseDateText(it)?.time?.let { d ->
                s.format(d)
            } ?: "解析失败"
            println("$ss   $it")
        }
    }

    @Test
    fun testParseDate2() {
        val rs = arrayOf(
                "%提前#分钟%"
                , "%提前#小时%"
                , "%提前#天%"
        )
        arrayOf("提前2分钟", "提前10分钟", "提前半小时", "提前一小时").forEach {
            rs.forEach f@{ r ->
                TextHelper.matchValues(it, r).also { re ->
                    if (re != null) {
                        println(it + " ${TextDateParser.toNum(re[1])}")
                        return@f
                    }
                }
            }
            TextHelper.matchValues(it, "%提前半小时%").also { re ->
                if (re != null)
                    println(30)
            }
        }
    }

    @Test
    fun testReg() {
        val m = TextHelper.matches("翻译屏幕", "(翻译%屏幕%)|(屏幕翻译)")
        println(m)
    }

    @Test
    fun htmlParse() {
//        val data = HttpBridge.get("https://www.coolapk.com/apk/cn.vove7.vassistant")

        val doc = Jsoup.connect("https://www.coolapk.com/apk/cn.vove7.vassistant").get()

        println(doc.body().getElementsByClass("list_app_info").text())
        println(doc.body().getElementsByClass("apk_left_title_info")[0].html().replace("<br> ", "\n"))

    }

    @Test
    fun tulingChat() {
        val a = TulingChatSystem().chatWithText("你好")
        println(a)
    }

    @Test
    fun subStr() {
        val voiceResult = "你好完毕"
        val it = "完毕"
        if (voiceResult.endsWith(it)) {
            println(voiceResult.substring(0, voiceResult.length - it.length))
        }
    }

    @Test
    fun testImageClassify() {
        val a = BaiduAipHelper.imageClassify("C:\\Users\\Administrator\\Desktop\\新建文件夹/home-bg-o.jpg")
        if (a?.hasErr == false) {
            println(a.bestResult)
        }
    }

    @Test
    fun testTranslate() {
        println(BaiduAipHelper.translate("你好\nGood", "auto", "auto")?.transResult)

    }

    @Test
    fun testKotlin() {
        val click = {
            if (1 == 2)
                println("1==2")
            else
                println("1!=2")
        }

        fff(click)

    }

    fun fff(onClick: (() -> Unit)? = null) {
        println(onClick)
        thread(isDaemon = true) {
            thread(isDaemon = true) {
                onClick?.invoke()
            }
        }
    }


    @Test
    fun testCountDown() {
        val c = CountDownLatch(2)
        c.countDown()
        thread {
            println(0)
            sleep(1000)
            println(1)
            c.countDown()
        }
        c.await()
        println("end")
    }

    @Test
    fun testInterrupt() {
        val t = thread {
            while (Thread.currentThread().isInterrupted.not()) {
                println(1)
            }
            println("isInterrupted")
        }
        sleep(1)
        t.interrupt()
        sleep(2000)
        println("end")
    }

    @Test
    fun testWhileWait() {
        var i = 0
        whileWaitTime(10000) {
            println(i++)
            return@whileWaitTime if (i > 50) Unit else null
        }

        whileWaitTime(100) {
            println(i++)
            sleep(5)
            return@whileWaitTime null
        }
        whileWaitCount(5) {
            println("whileWaitCount")
            return@whileWaitCount null //不反回结果
        }

        println("end")

    }

    @Test
    fun rangeTest() {
        val a = 5
        print(Range.create(0, 10).contains(a))
    }

    @Test
    fun subThreadTest() {
        thread(isDaemon = true) {
            //父线程
            thread {
                //子线程
                sleep(1000)
                if (!Thread.currentThread().isInterrupted)
                    println("未中断")
                else println("被中断")
                println("子线程结束")
            }
            println("父线程结束")
        }
        sleep(5000)
        println("end")
    }

    lateinit var bm: Bitmap
    @Test
    fun bitmap() {
        888.hex()
//        bm.getPixel(0,0)
//        val a = Color.green(0xffffffff)
//        print(a)
    }
}