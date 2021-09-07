package cn.vove7.jarvis.debug

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import cn.vove7.android.scaffold.ui.base.NoBindingActivity
import cn.vove7.common.bridges.SystemBridge

/**
 * # MediaControllerActivity
 *
 * @author Vove
 * @date 2021/6/20
 */
class MediaControllerActivity : NoBindingActivity() {

    val tv by lazy {
        TextView(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            Button(context).also {
                it.text = "PAUSE"
                setOnClickListener {
                    ctr(0)
                }
                addView(it)
            }
            Button(context).also {
                it.text = "RESUME/START"
                setOnClickListener {
                    ctr(1)
                }
                addView(it)
            }
            Button(context).also {
                it.text = "PRE"
                setOnClickListener {
                    ctr(2)
                }
                addView(it)
            }
            Button(context).also {
                it.text = "NEXT"
                setOnClickListener {
                    ctr(3)
                }
                addView(it)
            }
            addView(tv)
        })
    }

    private fun ctr(act: Int) {
        when (act) {
            0 -> SystemBridge.mediaPause()
            1 -> SystemBridge.mediaStart()
            2 -> SystemBridge.mediaPre()
            3 -> SystemBridge.mediaNext()
        }
    }

}