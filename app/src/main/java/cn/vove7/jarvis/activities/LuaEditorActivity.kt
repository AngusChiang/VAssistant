package cn.vove7.jarvis.activities

import android.os.Bundle
import cn.vove7.androlua.LuaHelper
import cn.vove7.androlua.luautils.LuaEditor
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.interfaces.CodeEditorOperation
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.CodeEditorActivity

class LuaEditorActivity : CodeEditorActivity() {

    override val codeEditor: CodeEditorOperation
            by lazy { findViewById<LuaEditor>(R.id.editor) }
    override val assetFolder: String = "lua_sample/"
    override val testFiles: Array<String> by lazy {
        assets.list("lua_sample")
    }
    override val scriptType: String = Action.SCRIPT_TYPE_LUA
    override val symbols: List<Symbol>
        get() = luaSymbols

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lua_code_editor)
    }

    init {
        LuaHelper.regPrint(print)
    }

    override fun onDestroy() {
        LuaHelper.unRegPrint(print)
        super.onDestroy()
    }
}