package cn.vove7.jarvis.view.dialog.base

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import cn.vove7.common.utils.runOnUi
import cn.vove7.jarvis.view.dialog.ProgressTextDialog
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.callbacks.onPreShow
import com.afollestad.materialdialogs.customview.customView

/**
 * # CustomizableDialog
 * 可定制
 * @author Administrator
 * 2018/11/25
 */
abstract class CustomizableDialog(
        val context: Context, val title: String? = null,
        val cancelable: Boolean = true,
        noAutoDismiss: Boolean = false, autoShow: Boolean = true) {
    val dialog by lazy {
        MaterialDialog(context)
                .title(text = title)
                .customView(view = initView(), scrollable = true)
                .cancelable(cancelable)
                .apply {
                    if (noAutoDismiss)
                        noAutoDismiss()
                    if (autoShow) show()
                }
    }
    val layoutInflater get() = LayoutInflater.from(context)

    open var message: String? = null
        get() = null

    abstract fun initView(): View


    @SuppressLint("CheckResult")
    fun positiveButton(
            @StringRes res: Int? = null,
            text: CharSequence? = null,
            click: DialogCallback? = null
    ): CustomizableDialog {
        dialog.positiveButton(res, text, click)
        return this
    }

    @SuppressLint("CheckResult")
    fun negativeButton(
            @StringRes res: Int? = null,
            text: CharSequence? = null,
            click: DialogCallback? = null
    ): CustomizableDialog {
        dialog.negativeButton(res, text, click)
        return this
    }

    @SuppressLint("CheckResult")
    fun neutralButton(
            @StringRes res: Int? = null,
            text: CharSequence? = null,
            click: DialogCallback? = null
    ): CustomizableDialog {
        dialog.neutralButton(res, text, click)
        return this
    }

    protected open fun onDis() {

    }

    fun onDismiss(callback: DialogCallback): CustomizableDialog {
        dialog.onDismiss {
            callback.invoke(it)
        }
        return this
    }

    fun dismiss() {
        dialog.dismiss()
    }

    fun show() {
        runOnUi {
            try {
                dialog.show()
            } catch (e: Exception) {
            }
        }
    }

    open fun onFinish() {}
    fun finish(posText: String? = null, onClick: (() -> Unit)? = null) {
        runOnUi {
            onFinish()
            dialog.positiveButton(text = posText) {
                onClick?.invoke()
                it.dismiss()
            }
        }
    }
}