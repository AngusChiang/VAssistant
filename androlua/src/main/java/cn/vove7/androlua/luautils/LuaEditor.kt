package cn.vove7.androlua.luautils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ActionMode
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.RadioGroup.LayoutParams
import android.widget.Toast
import cn.vove7.common.interfaces.CodeEditorOperation
import com.myopicmobile.textwarrior.android.FreeScrollingTextField
import com.myopicmobile.textwarrior.android.YoyoNavigationMethod
import com.myopicmobile.textwarrior.common.*
import com.myopicmobile.textwarrior.lua.LanguageLua

class LuaEditor(private val mContext: Context, attrs: AttributeSet)
    : FreeScrollingTextField(mContext, attrs), CodeEditorOperation {

    private val _inputingDoc: Document? = null

    private var _isWordWrap: Boolean = false

    private val _lastSelectedFile: String? = null

    //private String fontDir = "/sdcard/fonts/";

    private var _index: Int = 0
    private var finder: LinearSearchStrategy? = null
    private var idx: Int = 0
    private var mKeyword: String? = null

    override val selectedText: String?
        get() = _hDoc.subSequence(selectionStart, selectionEnd - selectionStart) as String
    override var isEdit: Boolean = false
        get() = isEdited

    val text: DocumentProvider
        get() = createDocumentProvider()

    init {
        setTypeface(Typeface.MONOSPACE)
        val dm = mContext.resources.displayMetrics
        //initFont();
        val size = TypedValue.applyDimension(2, FreeScrollingTextField.BASE_TEXT_SIZE_PIXELS.toFloat(), dm)
        setTextSize(size.toInt())
        isShowLineNumbers = true
        setHighlightCurrentRow(true)
        isWordWrap = false
        autoIndentWidth = 2
        Lexer.setLanguage(LanguageLua.instance)
        setNavigationMethod(YoyoNavigationMethod(this))
        val array = mContext.theme.obtainStyledAttributes(intArrayOf(android.R.attr.colorBackground, android.R.attr.textColorPrimary, android.R.attr.textColorHighlight))
        val backgroundColor = array.getColor(0, 0xFF00FF)
        val textColor = array.getColor(1, 0xFF00FF)
        val textColorHighlight = array.getColor(2, 0xFF00FF)
        array.recycle()
        setTextColor(textColor)
        setTextHighligtColor(textColorHighlight)
//        setDark(true)
        colorScheme = ColorSchemeDark()
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (_index != 0 && right > 0) {
            moveCaret(_index)
            _index = 0
        }
    }

    override fun getEditorContent(): String {
        return text.toString()
    }

    override fun setDark(isDark: Boolean) {
        if (isDark)
            colorScheme = ColorSchemeDark()
        else
            colorScheme = ColorSchemeLight()
    }

    fun addNames(names: Array<String>) {
        val lang = Lexer.getLanguage() as LanguageLua
        val old = lang.names
        val news = arrayOfNulls<String>(old.size + names.size)
        System.arraycopy(old, 0, news, 0, old.size)
        System.arraycopy(names, 0, news, old.size, names.size)
        lang.names = news
        Lexer.setLanguage(lang)
        respan()
        invalidate()

    }

    fun setPanelBackgroundColor(color: Int) {

        _autoCompletePanel.setBackgroundColor(color)
    }

    fun setPanelTextColor(color: Int) {

        _autoCompletePanel.setTextColor(color)
    }

    fun setKeywordColor(color: Int) {
        colorScheme.setColor(ColorScheme.Colorable.KEYWORD, color)
    }

    fun setUserwordColor(color: Int) {
        colorScheme.setColor(ColorScheme.Colorable.LITERAL, color)
    }

    fun setBasewordColor(color: Int) {
        colorScheme.setColor(ColorScheme.Colorable.NAME, color)
    }

    fun setStringColor(color: Int) {
        colorScheme.setColor(ColorScheme.Colorable.STRING, color)
    }

    fun setCommentColor(color: Int) {
        colorScheme.setColor(ColorScheme.Colorable.COMMENT, color)
    }

    fun setBackgoudColor(color: Int) {
        colorScheme.setColor(ColorScheme.Colorable.BACKGROUND, color)
    }

    fun setTextColor(color: Int) {
        colorScheme.setColor(ColorScheme.Colorable.FOREGROUND, color)
    }

    fun setTextHighligtColor(color: Int) {
        colorScheme.setColor(ColorScheme.Colorable.SELECTION_BACKGROUND, color)
    }

    override fun onKeyShortcut(keyCode: Int, event: KeyEvent): Boolean {
        val filteredMetaState = event.metaState and KeyEvent.META_CTRL_MASK.inv()
        if (KeyEvent.metaStateHasNoModifiers(filteredMetaState)) {
            when (keyCode) {
                KeyEvent.KEYCODE_A -> {
                    selectAll()
                    return true
                }
                KeyEvent.KEYCODE_X -> {
                    cut()
                    return true
                }
                KeyEvent.KEYCODE_C -> {
                    copy()
                    return true
                }
                KeyEvent.KEYCODE_V -> {
                    paste()
                    return true
                }
            }
        }
        return super.onKeyShortcut(keyCode, event)
    }

    override fun gotoLine() {
        startGotoMode()
    }

    override fun insert(s: CharSequence?) {
        insert(selectionStart, s)
    }

    override fun find() {
        startFindMode()
    }

    fun search() {
        startFindMode()
    }

    fun startGotoMode() {

        startActionMode(object : ActionMode.Callback {
            private var idx: Int = 0
            private var edit: EditText? = null

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                mode.title = "跳转行"
                mode.subtitle = null
                edit = object : androidx.appcompat.widget.AppCompatEditText(mContext) {
                    public override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if (s.length > 0) {
                            idx = 0
                            _gotoLine()
                        }
                    }
                }
                edit!!.setSingleLine(true)
                edit!!.inputType = 2
                edit!!.imeOptions = 2
                edit!!.setOnEditorActionListener { p1, p2, p3 ->
                    _gotoLine()
                    true
                }
                edit!!.layoutParams = LayoutParams(width / 3, -1)
                menu.add(0, 1, 0, "").actionView = edit
                menu.add(0, 2, 0, mContext.getString(android.R.string.ok))
                edit!!.requestFocus()
                return true
            }

            private fun _gotoLine() {
                val s = edit!!.text.toString()
                if (s.isEmpty())
                    return
                var l = Integer.valueOf(s)
                if (l > _hDoc.rowCount) {
                    l = _hDoc.rowCount
                }
                gotoLine(l)
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                when (item.itemId) {
                    1 -> {
                    }
                    2 -> _gotoLine()
                }
                return false
            }

            override fun onDestroyActionMode(p1: ActionMode) {}
        })
    }

    fun startFindMode() {
        startActionMode(object : ActionMode.Callback {
            private var finder: LinearSearchStrategy? = null
            private var idx: Int = 0
            private var edit: EditText? = null

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                mode.title = "查找"
                mode.subtitle = null
                edit = object : androidx.appcompat.widget.AppCompatEditText(mContext) {
                    public override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if (s.length > 0) {
                            idx = 0
                            findNext()
                        }
                    }
                }
                edit!!.setSingleLine(true)
                edit!!.imeOptions = 3
                edit!!.setOnEditorActionListener { p1, p2, p3 ->
                    findNext()
                    true
                }
                edit!!.layoutParams = LayoutParams(width / 3, -1)
                menu.add(0, 1, 0, "").actionView = edit
                menu.add(0, 2, 0, mContext.getString(android.R.string.search_go))
                edit!!.requestFocus()
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {

                when (item.itemId) {
                    1 -> {
                    }
                    2 -> findNext()
                }
                return false
            }

            private fun findNext() {
                finder = LinearSearchStrategy()
                val kw = edit!!.text.toString()
                if (kw.isEmpty()) {
                    selectText(false)
                    return
                }
                idx = finder!!.find(text, kw, idx, text.length, false, false)
                if (idx == -1) {
                    selectText(false)
                    Toast.makeText(mContext, "未找到", Toast.LENGTH_SHORT).show()
                    idx = 0
                    return
                }
                setSelection(idx, edit!!.text.length)
                idx += edit!!.text.length
                moveCaret(idx)
            }

            override fun onDestroyActionMode(p1: ActionMode) {}
        })

    }

    override fun setWordWrap(enable: Boolean) {
        _isWordWrap = enable
        super.setWordWrap(enable)
    }

    override fun setText(c: CharSequence) {
        //TextBuffer text=new TextBuffer();
        val doc = Document(this)
        doc.isWordWrap = _isWordWrap
        doc.setText(c)
        setDocumentProvider(DocumentProvider(doc))
        //doc.analyzeWordWrap();
    }

    fun insert(idx: Int, text: CharSequence?) {
        selectText(false)
        moveCaret(idx)
        paste(text?.toString() ?: "")
        //_hDoc.insert(idx,text);
    }

    fun setText(c: CharSequence, isRep: Boolean) {
        replaceText(0, length - 1, c.toString())
    }

    fun setSelection(index: Int) {
        selectText(false)
        if (!hasLayout())
            moveCaret(index)
        else
            _index = index
    }

    fun gotoLine(line: Int) {
        var line = line
        if (line > _hDoc.rowCount) {
            line = _hDoc.rowCount
        }
        val i = text.getLineOffset(line - 1)
        setSelection(i)
    }

    override fun undo() {
        val doc = createDocumentProvider()
        val newPosition = doc.undo()

        if (newPosition >= 0) {
            //TODO editor.setEdited(false); if reached original condition of file
            isEdited = true

            respan()
            selectText(false)
            moveCaret(newPosition)
            invalidate()
        }

    }

    override fun redo() {
        val doc = createDocumentProvider()
        val newPosition = doc.redo()

        if (newPosition >= 0) {
            isEdited = true
            respan()
            selectText(false)
            moveCaret(newPosition)
            invalidate()
        }

    }
    //
    //@Override
    //public void openFile(@NonNull String filename) {
    //    //_lastSelectedFile = filename;
    //    //
    //    //File inputFile = new File(filename);
    //    //_inputingDoc = new Document(this);
    //    //_inputingDoc.setWordWrap(this.isWordWrap());
    //    //ReadTask _taskRead = new ReadTask(this, inputFile);
    //    //_taskRead.start();
    //}


    fun findNext(keyword: String): Boolean {
        if (keyword != mKeyword) {
            mKeyword = keyword
            idx = 0
        }

        finder = LinearSearchStrategy()
        val kw = mKeyword
        if (kw!!.isEmpty()) {
            selectText(false)
            return false
        }
        idx = finder!!.find(text, kw, idx, text.length, false, false)
        if (idx == -1) {
            selectText(false)
            Toast.makeText(mContext, "未找到", Toast.LENGTH_SHORT).show()
            idx = 0
            return false
        }
        setSelection(idx, mKeyword!!.length)
        idx += mKeyword!!.length
        moveCaret(idx)
        return true
    }

}
