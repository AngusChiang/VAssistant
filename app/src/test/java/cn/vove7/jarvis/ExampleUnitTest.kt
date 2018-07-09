package cn.vove7.jarvis

import org.junit.Test

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

}