package cn.vove7.executorengine

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
        f(1, "2", 3,"4")
        f(2)
    }

    fun f(a: Any, vararg ss: Any) {
        if (ss.isNotEmpty())
            println(ss.asList())
    }
}