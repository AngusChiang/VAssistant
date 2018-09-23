package cn.vove7.jarvis

import cn.vove7.vtp.reflect.ReflectHelper
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

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

        val c = SimpleDateFormat("HH:mm:ss")
        c.format(Date())
        val b = B()

        val s = "sss"

        A<C>("").insB()
    }

    class C : B() {
        val s = ""
    }

    class A<T : B>(val s: String) {
        var d: T? = null
        fun insB() {
            d = ReflectHelper.newInstance<B>(B::class.java, arrayOf()) as T
            println(d?.data)
        }
    }

    open class B {
        val data = "sss"

    }
}