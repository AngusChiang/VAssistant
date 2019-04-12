package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.view.View
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.show
import cn.vove7.jarvis.R
import cn.vove7.jarvis.view.dialog.base.CustomizableDialog
import kotlinx.android.synthetic.main.dialog_loading.view.*

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
                    val msg: String? = null)
    : CustomizableDialog(context, title, cancelable, noAutoDismiss) {

    private val hBar
            by lazy { v.horizontal_bar }
    private val rBar
            by lazy { v.round_bar }
    private val msgView
            by lazy { v.msg_view }
    private val v
            by lazy { layoutInflater.inflate(R.layout.dialog_loading, null) }

    var progress: Int = 0
        set(value) {
            if (horizontal) {
                hBar.isIndeterminate = false
                hBar.progress = value
            } else {
                hBar.isIndeterminate = false
                rBar.progress = value
            }
            field = value
        }

    override var message: String?
        get() = msgView.text.toString()
        set(value) {
            msgView.text = value
        }

    override fun initView(): View {
        msgView.text = msg
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