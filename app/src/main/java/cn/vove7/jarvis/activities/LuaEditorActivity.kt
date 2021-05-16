package cn.vove7.jarvis.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import cn.vove7.androlua.LuaHelper
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.interfaces.CodeEditorOperation
import cn.vove7.jarvis.activities.base.CodeEditorActivity
import cn.vove7.jarvis.databinding.ActivityLuaCodeEditorBinding

class LuaEditorActivity : CodeEditorActivity<ActivityLuaCodeEditorBinding>() {

    override val codeEditor: CodeEditorOperation by lazy { viewBinding.editor }
    override val assetFolder: String = "lua_sample/"
    override val testFiles: Array<String> by lazy {
        assets.list("lua_sample") ?: emptyArray()
    }
    override val scriptType: String = Action.SCRIPT_TYPE_LUA
    override val symbols: List<Symbol>
        get() = luaSymbols

    init {
        LuaHelper.regPrint(print)
    }

    override fun onDestroy() {
        LuaHelper.unRegPrint(print)
        super.onDestroy()
    }
}