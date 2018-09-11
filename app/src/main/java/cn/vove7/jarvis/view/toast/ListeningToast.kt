package cn.vove7.jarvis.view.toast

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
import java.lang.Thread.sleep
import kotlin.concurrent.thread

/**
 * # ListeningToast
 * 无限显示
 * @author 17719247306
 * 2018/9/9
 */
class ListeningToast(context: Context) {
    var toast: Toast = Toast(context)

    var toastView: View = LayoutInflater.from(context).inflate(R.layout.toast_listening_text, null)
    var textView: TextView = toastView.findViewById(R.id.text)

    var isHide = false
    val lHandler: ListeningHandler

    init {
        toast.view = toastView
        toast.setGravity(Gravity.TOP, 0, 10)
        lHandler = ListeningHandler(Looper.myLooper())
    }

    var timerThread: Thread? = null
    fun show(text: String) {
        isHide = false
        lHandler.sendMessage(lHandler.obtainMessage(SHOW, text))
    }

    fun showAndHideDelay(text: String) {
        lHandler.sendMessage(lHandler.obtainMessage(SHOW, text))
        hideDelay()
    }

    fun hideImmediately() {//立即
        Vog.v(this, "hideDelay ---> hide exec")
        synchronized(this@ListeningToast) {
            //            while (timerThread != null && !timerThread!!.isInterrupted)
            timerThread?.interrupt()
            isHide = true
            toast.cancel()
        }
    }

    inner class ListeningHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                SHOW -> {
                    val text = msg.obj as String
                    synchronized(this@ListeningToast) {
                        timerThread?.interrupt()
                        textView.text = text
                        timerThread = thread {
                            //刷新，阻止消失
                            try {
                                while (!isHide && !Thread.currentThread().isInterrupted) {
                                    toast.duration = Toast.LENGTH_LONG
                                    toast.show()
                                    Vog.v(this, "handleMessage ---> show toast $text")
                                    sleep(500)
                                }
                            } catch (e: InterruptedException) {
                                Vog.v(this, "handleMessage ---> interrupt")
                                Thread.currentThread().interrupt()
                            }//结束
                            finally {
                                timerThread = null
                            }
                        }
                    }
                }
                HIDE -> {
                    hideImmediately()
                }
                else -> {
                }
            }

        }

    }

    companion object {
        const val SHOW = 5
        const val HIDE = 1
    }

    fun hideDelay(delay: Long = 800) {
        Vog.d(this, "hideDelay ---> hide delay $delay")
        lHandler.sendEmptyMessageDelayed(HIDE, delay)
    }

}