package cn.vove7.jarvis.debug

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.bridges.GlobalActionExecutor
import cn.vove7.common.bridges.ShellHelper
import java.lang.Thread.sleep
import kotlin.concurrent.thread

/**
 * # ClickCountActivity
 *
 * @author Vove
 * @date 2021/8/16
 */
class ClickCountActivity : AppCompatActivity() {

    var c = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this)

        val frameLayout = FrameLayout(this)

        tv.text = "0"
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
        tv.gravity = Gravity.CENTER
        frameLayout.addView(tv, FrameLayout.LayoutParams(-1, -1))
        frameLayout.addView(Button(this).apply {
            text = "RUN"
            setOnClickListener {
                run()
            }
        }, FrameLayout.LayoutParams(-2, -2))
        setContentView(frameLayout)
        tv.setOnClickListener {
            c++
            tv.text = "$c"
        }
    }

    var t: Thread? = null
    private fun run() {
        if (t != null) {
            t?.interrupt()
            t = null
            return
        }
        t = thread {
            val tt = Thread.currentThread()
            while (t!=null&&!tt.isInterrupted) {
                try {
                    if (AccessibilityApi.gestureService != null) {
                        GlobalActionExecutor.click(200, 1000)
                    } else {
                        ShellHelper.execWithAdb("input tap 200 1000", false, false)
                        sleep(5)
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    break
                }
            }
            ShellHelper.release()
            t = null
        }
    }
}