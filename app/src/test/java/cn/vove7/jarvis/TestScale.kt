package cn.vove7.jarvis

import org.junit.Test

/**
 * @author 17719247306
 *
 *
 * 2018/8/16
 */
class TestScale {
    @Test
    fun aaa() {
        val w = 1080
        val h = 1920
        val x = 358.0
        val y = 596.0
        val fx = x / w
        val fy = y / h
        println(fx)
        println(fy)
        println(fx * w)
        println(fy * h)
    }
}
