package cn.vove7.common.view.editor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import android.widget.EditText
import cn.vove7.common.view.editor.codeparse.AbsLexicalAnalyzer
import cn.vove7.common.view.editor.codeparse.JsLexicalAnalyzer
import cn.vove7.common.view.editor.codeparse.Word
import cn.vove7.common.R


/**
 * # CodeTextView
 *
 * @author Administrator
 * 2018/10/9
 */
open class CodeTextView : EditText {
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

    protected open fun init() {
        post {
            setHorizontallyScrolling(true)
        }
        setTextIsSelectable(true)
        isFocusable = true
        line.color = Color.GRAY
        line.strokeWidth = 2f
        setPadding(95, 0, 0, 0)
        gravity = Gravity.TOP

    }

    val line = Paint()

    override fun onDraw(canvas: Canvas?) {
        if (text.isNotEmpty()) {
            var y = 0f
            val p = Paint()
            p.color = Color.GRAY
            p.textSize = 40f
            for (l in 0 until lineCount) {
                y = (((l + 1) * lineHeight) - (lineHeight / 4)).toFloat()
                canvas?.drawText(((l + 1).toString()), 0f, y, p)
                canvas?.save()
            }
        }
        val k = lineHeight
        val i = lineCount
//        canvas?.drawLine(90f, 0f, 90f, (height + (i * k)).toFloat(), line);
        val y = (layout.getLineForOffset(selectionStart) + 1) * k;
        canvas?.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), line)
        canvas?.save()
        canvas?.restore()
        super.onDraw(canvas)
    }


    var lang: String? = null
    open fun setCode(code: String, lang: String) {
        this.lang = lang
        val analyzer: AbsLexicalAnalyzer = when (lang) {
            "javascript" -> JsLexicalAnalyzer()
            "lua" -> JsLexicalAnalyzer()
            else -> JsLexicalAnalyzer()
        }
        analyzer.analysis(code)
        val words = analyzer.wordList
        if (words != null) {
            renderCode(words)
        } else {
            setText(code)
        }
        setSelection(0)
    }

    private fun renderCode(words: List<Word>) {
        text.clear()
        var cRow = 1
        var cCol = 0
        words.forEach {
            val dr = it.row - cRow
            if (dr != 0) {//新行
                for (i in 0 until dr)
                    append("\n")
                cCol = 0
                cRow = it.row
            }
            //添加 空格
            val dif = it.start - cCol
            if (dif > 0) {
                for (i in 0 until dif) {
                    append(" ")
                }
            }
            //渲染字体
            if (it.error) {
                append(MultiSpan(context, it.word, colors[0], underLine = true).spanStr)
            } else {//
                append(MultiSpan(context, it.word, colors[it.wordType]).spanStr)
            }
            cCol = it.end
        }
    }

    /**
     * wordType：
     * 1：关键字
     * 2：分界符
     * 3：算数运算符
     * 4：关系运算符
     * 5：常数
     * 6：标识符
     * 7：字符串
     */
    private val colors = arrayOf(
            R.color.red_900,
            R.color.blue_900,
            R.color.brown_800,
            R.color.brown_800,
            R.color.orange_900,
            R.color.amber_A700,
            R.color.indigo_700,
            R.color.yellow_700

    )


}