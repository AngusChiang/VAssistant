package cn.vove7.jarvis.view.dialog.base

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.View
import cn.vove7.jarvis.fragments.base.BaseBottomDialogWithToolbar
import cn.vove7.jarvis.view.custom.WrappedTextView
import cn.vove7.jarvis.R

/**
 * # BottomDialogWithText
 *
 * @author Administrator
 * 2018/12/19
 */
class BottomDialogWithText(context: Context, title: String, val sourceText: String? = null, val horizontalScroll: Boolean = false)
    : BaseBottomDialogWithToolbar(context, title) {
    val textView by lazy { WrappedTextView(context) }
    override fun onCreateContentView(parent: View): View {
        textView.apply {
            setPadding(20, 20, 20, 20)
            text = sourceText
            setTextColor(context.resources.getColor(R.color.primary_text))
            textSize = 15f
            if (horizontalScroll) {
                setHorizontallyScrolling(true)
                isFocusable = false
                isHorizontalScrollBarEnabled = true
                movementMethod = ScrollingMovementMethod.getInstance()
            }
        }

        return textView
    }
}