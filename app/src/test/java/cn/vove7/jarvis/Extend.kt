package cn.vove7.jarvis

import org.junit.Test

/**
 * # Extend
 *
 * @author Vove
 * 2019/6/23
 */
class Extend {
    @Test
    fun main() {
        A()
    }
}

class A : B() {
    init {
        print("A")
    }
}

open class B {
    init {
        print("B")
    }
}