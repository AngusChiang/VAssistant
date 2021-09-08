package cn.vove7.jarvis.debug

import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import cn.vove7.common.app.withFailLog
import cn.vove7.common.bridges.ScrcpyActionExecutor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
            ::testGesture,
            ::testPerformAcsAction,
            ::takeScreenshot,
            ::powerDialog,
            ::splitScreen,
        ))
        lv.setOnItemClickListener { _, _, position, _ ->
            thread {
                kotlin.runCatching {
                    (lv.adapter as ArrayAdapter<KCallable<*>>)
                        .getItem(position)?.also {
                            it.isAccessible = true
                            it.call()
                        }
                }.withFailLog()
                sleep(800)
                Log.d("ScrcpyTest", "exec end")
                ScrcpyActionExecutor.release()
            }
        }
        setContentView(lv)
    }

    private fun testHomeKey() {
        ScrcpyActionExecutor.home()
    }

    private fun testClick0_0() {
        ScrcpyActionExecutor.click(0, 0)
    }

    private fun testGesture() {
        ScrcpyActionExecutor.home()
        sleep(1000)
        ScrcpyActionExecutor.gestures(500,
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

    private fun testPerformAcsAction() = GlobalScope.launch {
        ScrcpyActionExecutor.notificationBar()
        delay(1000)
        ScrcpyActionExecutor.back()
        delay(1000)
        ScrcpyActionExecutor.quickSettings()
    }

    private fun takeScreenshot() {
        ScrcpyActionExecutor.screenShot()
    }

    private fun powerDialog() {
        ScrcpyActionExecutor.powerDialog()
    }

    private fun splitScreen() {
        ScrcpyActionExecutor.splitScreen()
    }

}