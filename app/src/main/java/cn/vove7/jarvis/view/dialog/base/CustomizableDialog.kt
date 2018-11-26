package cn.vove7.jarvis.view.dialog.base

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.utils.runOnUi
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
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
        noAutoDismiss: Boolean = false) {
    val dialog by lazy {
        MaterialDialog(context)
                .title(text = title)
                .cancelable(cancelable)
                .apply {
                    if (noAutoDismiss)
                        noAutoDismiss()
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
                dialog.customView(view = initView(), scrollable = true)
                dialog.show()
            } catch (e: Exception) {
            }
        }
    }

    open fun onFinish() {}
    fun finish(posText: String? = null, onClick: (() -> Unit)? = null) {
        onFinish()
        try {
            dialog.positiveButton(text = posText) {
                onClick?.invoke()
                it.dismiss()
            }.show()
        } catch (e: Exception) {
            GlobalLog.err(e, "cd107")
            e.printStackTrace()
        }
    }
}