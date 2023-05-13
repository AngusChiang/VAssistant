package cn.vove7.jarvis

import org.junit.Test
import java.lang.Math.max
import java.util.*

/**
 * @author Vove
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

    @Test
    fun calendar() {
        val c = Calendar.getInstance()
        print(c.get(Calendar.DAY_OF_WEEK) - 1)
    }


    @Test
    fun test() {
        arrayOf("helloworld", "abcabcbb", "bbbbb", "pwwkew").forEach {
            println("$it  ${deal(it)}")
        }
    }

    private fun deal(s: String): Int {
        var i = 0
        var max = 0
        val map = hashMapOf<Char, Int>() //记录 字符对应 索引
        while (i < s.length) {
            val c = s[i]
            if (map.contains(c)) {//包含
                // 求最长
                max = max(max, map.size)
                // i 索引指到 原s[i] + 1 位置
                i = (map[s[i]] ?: i) + 1
                map.clear()
                map[s[i]] = i  //put
            } else {//不包含
                map[c] = i
            }
            i++
        }
        max = max(max, map.size)
        return max
    }
}
