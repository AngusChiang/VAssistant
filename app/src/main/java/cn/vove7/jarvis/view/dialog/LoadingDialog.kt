package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.show
import cn.vove7.jarvis.view.dialog.base.CustomizableDialog
import cn.vove7.jarvis.R

/**
 * # LoadingDialog
 *
 * @author Administrator
 * 2018/11/25
 */
/**
 *
 * @property msg String?
 * @property hBar ProgressBar
 * @property rBar ProgressBar
 * @property msgView TextView?
 * @property progress Int
 * @property message String?
 * @constructor
 */
class LoadingDialog(context: Context, title: String?,
                    cancelable: Boolean = false,
                    val horizontal: Boolean = false,
                    noAutoDismiss: Boolean = false,
                    val msg: String? = null, autoShow: Boolean = false)
    : CustomizableDialog(context, title, cancelable, noAutoDismiss, autoShow) {

    lateinit var hBar: ProgressBar
    lateinit var rBar: ProgressBar
    var msgView: TextView? = null

    var progress: Int = 0
        set(value) {
            if (horizontal) {
                hBar.isIndeterminate = false
                hBar.progress = value
            } else {
                hBar.isIndeterminate = false
                rBar.progress = value
            }
        }

    override var message: String?
        get() = msgView?.text.toString()
        set(value) {
            msgView?.text = value
        }

    override fun initView(): View {
        val v = layoutInflater.inflate(R.layout.dialog_loading, null)
        hBar = v.findViewById(R.id.horizontal_bar)
        rBar = v.findViewById(R.id.round_bar)
        msgView = v.findViewById(R.id.msg_view)
        msgView?.text = msg

        if (horizontal) {
            hBar.show()
            rBar.gone()
        } else {
            rBar.show()
            hBar.gone()
        }
        return v
    }

    override fun onFinish() {
        rBar.gone()
    }
}