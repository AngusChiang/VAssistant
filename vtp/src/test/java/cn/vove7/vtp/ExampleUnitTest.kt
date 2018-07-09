package cn.vove7.vtp

import cn.vove7.vtp.text.TextHelper
import org.junit.Assert.assertEquals
import org.junit.Test

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

    @Test
    fun transformWords2Chinese() {
        val wws = arrayOf(
                "你好",
                "刘学他",
                "刘雪涛",
                "刘薛涛",
                "刘xuetao"
        )
        wws.forEach {
            println(TextHelper.chineseStr2Pinyin(it))
        }
    }

    @Test
    fun compareSimilarityTest() {
        val tests = hashMapOf(
                Pair("刘学他", "刘雪涛"),
                Pair("刘雪涛", "刘雪韬"),
                Pair("刘雪", "刘雪韬"),
                Pair("你好", "您好"),
                Pair("Qq", "qq"),
                Pair("李本", "李严"),
                Pair("点击RUN", "RUN")


        )
        tests.forEach {
            println("""${it.key} ${it.value}""")
            println(TextHelper.compareSimilarityWithPinyin(it.key, it.value))
            println()
        }
    }

    @Test
    fun testChinese2First() {
        arrayOf(
                "一二三",
                "i我和欧文h",
                "吗朦胧"
        ).forEach {
            println(TextHelper.chineseStr2Pinyin(it,true))
        }
    }


}