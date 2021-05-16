package cn.vove7.jarvis.view.dialog.contentbuilder

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import cn.vove7.bottomdialog.interfaces.ContentBuilder
import cn.vove7.bottomdialog.util.ObservableList
import cn.vove7.bottomdialog.util.fadeIn
import cn.vove7.bottomdialog.util.listenListToUpdate
import cn.vove7.common.utils.fadeOut
import cn.vove7.common.utils.runInCatch
import cn.vove7.common.utils.runOnUi
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.baiduaip.BaiduAipHelper
import cn.vove7.jarvis.view.custom.SlideFlexboxLayout
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 分词 内容构造器
 * @property rawWords String
 * @property layoutRes Int
 * @property wordList ObservableList<String>
 * @property selectedTextList MutableList<CheckedTextView>
 * @property loadingBar ProgressBar
 * @property content WarpLinearLayout
 * @property loading Boolean
 * @property type Int
 * @constructor
 */
class WordSplitBuilder(
        scope: CoroutineScope,
        private val rawWords: String
) : ContentBuilder() {

    override val layoutRes: Int = R.layout.dialog_pick_text

    private val wordList by listenListToUpdate(ObservableList.build<String> { }, this)
    private val selectedTextList = mutableListOf<TextView>()

    private lateinit var loadingBar: ProgressBar
    private lateinit var content: SlideFlexboxLayout

    private var loading: Boolean = false
        set(value) {
            runOnUi {
                runInCatch {
                    if (value) loadingBar.fadeIn()
                    else loadingBar.fadeOut(endStatus = View.INVISIBLE)
                }
            }
            field = value
        }

    private var lexerJob: Job?

    init {
        loading = true
        lexerJob = scope.launch {
            BaiduAipHelper.lexer(rawWords).also {
                loading = false
                if (it == null) {
                    wordList.add("分词失败")
                } else {
                    wordList.addAll(it)
                }
                lexerJob = null
            }
        }
    }

    override fun init(view: View) {
        loadingBar = view.findViewById(R.id.loading_bar)
        content = view.findViewById(R.id.words_content)

        if (loading) loadingBar.fadeIn()
        else loadingBar.fadeOut()
        dialog.setOnDismissListener {
            lexerJob?.cancel()
        }
    }

    override fun updateContent(type: Int, data: Any?) {
        content.removeAllViews()
        wordList.forEachIndexed { i, v ->
            val vv = buildView(i, v)
            content.addView(vv)
            selectedTextList.add(vv)
        }
    }


    private fun buildView(index: Int, it: String): TextView {
        return TextView(dialog.context).apply {
            setBackgroundResource(R.drawable.pickable_bg)
            text = it
            setPadding(20, 10, 20, 10)
            layoutParams = FlexboxLayout.LayoutParams(-2, -2).also {
                it.setMargins(6, 6, 6, 6)
            }
            textSize = 16f
            setTextColor(
                    ColorStateList(
                            arrayOf(
                                    intArrayOf(android.R.attr.state_selected),
                                    intArrayOf()
                            ),
                            intArrayOf(Color.WHITE, Color.BLACK)
                    )
            )
            isFocusable = false
            tag = index
        }
    }

    /**
     * 请求父级是否拦截Touch事件
     * @param targetView View
     * @param disAllow Boolean
     */
    private fun requestDisIntercept(targetView: View, disAllow: Boolean) {
        var par = targetView.parent
        while (par != null) {
            par.requestDisallowInterceptTouchEvent(disAllow)
            par = par.parent
        }
    }

    /**
     * 获得选取连接文本
     * @param list List<CheckedTextView>
     * @return String
     */
    fun getCheckedText(): String = buildString {
        selectedTextList.forEach {
            if (it.isSelected) {
                append(it.text)
            }
        }
    }

}