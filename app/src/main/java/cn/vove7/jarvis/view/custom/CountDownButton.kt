package cn.vove7.jarvis.view.custom

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.widget.Button
import cn.vove7.vtp.log.Vog

/**
 * # CountDownButton
 *
 * @author Administrator
 * 2018/9/14
 */

typealias OnFinish = () -> Unit

class CountDownButton : Button {
    constructor(context: Context?)
            : super(context)

    constructor(context: Context?, attrs: AttributeSet?)
            : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private var preText: String? = null
    val RecOnFinish: OnFinish = {
        isEnabled = true
        this.text = preText
    }

    val handler = CountDownHandler(this)
    fun startDown(beginSecs: Int, enable: Boolean = false) {
        if (preText == null) {
            preText = this.text.toString()
        }

        this.isEnabled = enable
        if (stoped) {
            stoped = false
            handler.sendMessage(handler.obtainMessage(0, beginSecs))
        } else handler.currentCount = beginSecs
    }

    fun setFormat(s: String) {
        handler.s = s
    }

    var callback: OnFinish = RecOnFinish

    fun setCallBack(call: OnFinish) {
        callback = call
    }

    var stoped = true

    fun pause() {
        stoped = true
    }

    fun resume() {
        stoped = false
        handler.sendEmptyMessage(1)
    }

    class CountDownHandler(val btn: CountDownButton, var s: String = "%ds") : Handler() {

        var currentCount = 0
            set(value) {
                btn.text = String.format(s, value)
                field = value
            }

        fun setText() {

        }

        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                0 -> {//begin
                    currentCount = msg.obj as Int
                    btn.text = String.format(s, currentCount--)
                    this.sendEmptyMessageDelayed(1, 1000)
                }
                1 -> {//resume
                    if (!btn.stoped) {
                        if (currentCount >= 0) {
                            btn.text = String.format(s, currentCount--)
                            Vog.d("handleMessage ---> $currentCount")
                            this.sendEmptyMessageDelayed(1, 1000)
                        } else {//结束
                            btn.stoped = true
                            btn.callback.invoke()
                        }
                    }
                }
            }
        }
    }

}