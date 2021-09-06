package cn.vove7.jarvis.debug

import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import cn.vove7.common.bridges.AdbActionExecutor
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.reflect.KCallable
import kotlin.reflect.jvm.isAccessible

/**
 * # ScrcpyTestActivity
 *
 * @author Vove
 * @date 2021/9/6
 */
class ScrcpyTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lv = ListView(this)
        lv.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
            android.R.id.text1, listOf(
            ::testHomeKey,
            ::testClick0_0,
            ::testGesture
        ))
        lv.setOnItemClickListener { _, _, position, _ ->
            thread {
                (lv.adapter as ArrayAdapter<KCallable<*>>)
                    .getItem(position)?.also {
                        it.isAccessible = true
                        it.call()
                    }
                Log.d("ScrcpyTest", "exec end")
                AdbActionExecutor.release()
            }
        }
        setContentView(lv)
    }

    private fun testHomeKey() {
        AdbActionExecutor.home()
    }

    private fun testClick0_0() {
        AdbActionExecutor.click(0, 0)
    }

    private fun testGesture() {
        AdbActionExecutor.home()
        sleep(1000)
        AdbActionExecutor.gestures(500,
            arrayOf(
                arrayOf(
                    Pair(300, 300),
                    Pair(600, 600),
                ),
                arrayOf(
                    Pair(1000, 1200),
                    Pair(700, 700),
                )
            )
        )
    }

}