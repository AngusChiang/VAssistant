package cn.vove7.jarvis

import cn.vove7.vtp.reflect.ReflectHelper
import org.junit.Test

/**
 * # NewT
 *
 * @author 17719
 * 2018/8/14
 */
class NewT {

    @Test
    fun test() {
        //new A() by reflect

        val b = B()
        val s = "sss"

        A<B>("").insB()
    }


    class A<T : B>(val s: String) {
        var d: T? = null
        fun insB() {
            d = ReflectHelper.newInstance<B>(B::class.java, arrayOf()) as T
            println(d?.data)
        }
    }

    class B {
        val data = "sss"

    }
}