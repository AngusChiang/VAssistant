package cn.vove7.jarvis.fragments.base

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import cn.vove7.common.utils.gone
import cn.vove7.common.utils.show
import cn.vove7.jarvis.R


/**
 * # BaseBottomDialogWithToolbar
 *
 * @author Administrator
 * 9/21/2018
 */
abstract class BaseBottomDialogWithToolbar(context: Context, title: String = "") : BottomSheetDialog(context) {
    //
    private val mContentView: View by lazy { View.inflate(context, R.layout.bottom_dialog_with_toolbar, null) }
    val contentContainer: ViewGroup by lazy { mContentView.findViewById<ViewGroup>(R.id.content_container) }
    private val pbar by lazy { mContentView.findViewById<ProgressBar>(R.id.p_bar) }

    private val buttonPositive: TextView by lazy { mContentView.findViewById<TextView>(R.id.dialog_button_positive) }
    private val buttonNegative: TextView by lazy { mContentView.findViewById<TextView>(R.id.dialog_button_negative) }
    private val buttonNeutral: TextView by lazy { mContentView.findViewById<TextView>(R.id.dialog_button_neutral) }

    var autoDismiss = true

    val toolbar: Toolbar by lazy {
        mContentView.findViewById<Toolbar>(R.id.toolbar).also {
            it.title = title
        }
    }

    var title: String? = title
        set(value) {
            toolbar.title = value
            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(mContentView)
        delegate.setSupportActionBar(toolbar)
//        toolbar.setNavigationOnClickListener { dismiss() }
        val v = onCreateContentView(contentContainer)
        contentContainer.addView(v)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setOnDismissListener { listener?.onDismiss() }
    }

    fun showLoadingBar() {
        pbar.show()
    }

    fun hideLoadingBar() {
        pbar.gone()
    }

    abstract fun onCreateContentView(parent: View): View

    fun transparent() {
        (mContentView.parent as View).setBackgroundColor(context.resources.getColor(android.R.color.transparent))
    }

//    fun noDarkBackground() {
//        (BottomSheetDialogFragment.STYLE_NORMAL, R.style.TransBottomSheetDialogStyle)
//    }


    fun positiveButton(text: CharSequence? = null, click: (() -> Unit)? = null) {
        setButton(buttonPositive, text, click)
    }

    fun negativeButton(text: CharSequence? = null, click: (() -> Unit)? = null) {
        setButton(buttonNegative, text, click)
    }

    fun neutralButton(text: CharSequence? = null, click: (() -> Unit)? = null) {
        setButton(buttonNeutral, text, click)
    }

    private fun setButton(which: TextView, text: CharSequence? = null, click: (() -> Unit)? = null) {
        which.show()
        which.text = text
        which.setOnClickListener {
            click?.invoke()
            if (autoDismiss) {
                dismiss()
            }
        }
    }

    fun noAutoDismiss() {
        autoDismiss = false
    }

    var listener: SheetStatusListener? = null
}

interface SheetStatusListener {
    fun onDismiss()
}