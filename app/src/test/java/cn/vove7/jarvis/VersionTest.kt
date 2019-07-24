package cn.vove7.jarvis

import org.junit.Test

/**
 * # VersionTest
 *
 * @author Vove
 * 2019/7/24
 */
class VersionTest {
    @Test
    fun main() {
        assert(version2Int("1.9.2") < version2Int("1.9.2.1"))
        assert(version2Int("1.9.3") == version2Int("1.9.3"))
        assert(version2Int("1.3.3") < version2Int("1.9.3"))
        assert(version2Int("1.3.3.3.3.3") > version2Int("1.3.1.9.1.1.1.1.1"))
    }
}

private fun version2Int(s: String): Float {
    var sum = 0f
    var t = 1000000f
    s.split('.').forEach {
        sum += it.toInt() * t
        t /= 100
    }
    return sum.also {
        print( "$it  ***  ")
    }
}