package cn.vove7.jarvis.view.dialog

import android.content.Context
import android.view.View
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.show
import cn.vove7.jarvis.databinding.DialogLoadingBinding
import cn.vove7.jarvis.view.dialog.base.CustomizableDialog

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

    private inline val hBar get() = viewBinding.horizontalBar

    private inline val rBar get() = viewBinding.roundBar

    private val msgView get() = viewBinding.msgView

    private val viewBinding by lazy {
        DialogLoadingBinding.inflate(layoutInflater)
    }

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
        return viewBinding.root
    }

    override fun onFinish() {
        rBar.gone()
    }
}