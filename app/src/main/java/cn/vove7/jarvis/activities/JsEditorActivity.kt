package cn.vove7.jarvis.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.interfaces.CodeEditorOperation
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.CodeEditorActivity
import cn.vove7.jarvis.databinding.ActivityJsCodeEditorBinding
import cn.vove7.rhino.api.RhinoApi
import cn.vove7.rhino.processor.TextProcessor
import cn.vove7.rhino.processor.language.MyJsLanguageWithApi
import cn.vove7.rhino.processor.widget.FastScrollerView
import cn.vove7.rhino.processor.widget.GutterView

class JsEditorActivity : CodeEditorActivity<ActivityJsCodeEditorBinding>() {

    override val assetFolder: String = "js_sample/"
    override val scriptType: String = Action.SCRIPT_TYPE_JS

    override val codeEditor: CodeEditorOperation by lazy {
        viewBinding.editor
    }
    override val testFiles: Array<String> by lazy {
        assets.list("js_sample") ?: emptyArray()
    }
    override val symbols: List<Symbol>
        get() = jsSymbols

    init {
        RhinoApi.regPrint(print)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mEditor = viewBinding.editor
        mEditor.language = MyJsLanguageWithApi()
        mEditor.init()

        val mFastScrollerView = findViewById<FastScrollerView>(R.id.fast_scroller)
        mFastScrollerView.link(mEditor) //подключаем FastScroller к редактору

        val mGutterView = findViewById<GutterView>(R.id.gutter)
        mGutterView.link(mEditor) //подключаем Gutter к редактору
        mEditor.setHighlightCurrentLine(true)
        mEditor.enableUndoRedoStack() //включаем Undo/Redo ПОСЛЕ открытия файла

        mEditor.setHorizontallyScrolling(true)
        mEditor.refreshEditor() //подключаем все настройки
    }

    override fun onDestroy() {
        RhinoApi.unregPrint(print)
        super.onDestroy()
    }

}
