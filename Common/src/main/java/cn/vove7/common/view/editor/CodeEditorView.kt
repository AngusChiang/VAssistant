package cn.vove7.common.view.editor

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity

/**
 * # CodeEditorView
 *
 * @author Administrator
 * 2018/10/9
 */
@Deprecated("待优化")
class CodeEditorView : CodeTextView {
    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?)
            : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init()
    }

    var fromUser = false

    override fun init() {
        super.init()
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (fromUser) {
                    removeCallbacks(reCode)
                    postDelayed(reCode, delayRender)
                }
            }
        })
        setBackgroundColor(context.resources.getColor(android.R.color.white))
        gravity = Gravity.TOP
    }

    private val reCode = Runnable {
        setCode(text.toString(), lang ?: "")
    }

    var delayRender = 500L
    override fun setCode(code: String, lang: String) {
        val b = selectionStart
        fromUser = false
        super.setCode(code, lang)

        setSelection(Math.min(b, text.length - 1))
        fromUser = true
    }

}