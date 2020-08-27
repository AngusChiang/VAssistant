package cn.vove7.jarvis.activities.base

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import cn.vove7.androlua.luabridge.LuaUtil
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.model.ExecutorStatus
import cn.vove7.common.appbus.model.ExecutorStatus.Companion.ON_EXECUTE_FINISHED
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.executor.OnPrint
import cn.vove7.common.interfaces.CodeEditorOperation
import cn.vove7.common.utils.color
import cn.vove7.common.utils.spanColor
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.UriUtils
import cn.vove7.jarvis.view.EditorFunsHelper
import cn.vove7.jarvis.view.dialog.ProgressTextDialog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import kotlinx.android.synthetic.main.editor_tool_bar.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * # CodeEditorActivity
 *
 * @author Administrator
 * 2018/10/9
 */
abstract class CodeEditorActivity : BaseActivity() {

    abstract val codeEditor: CodeEditorOperation
    override val darkTheme: Int
        get() = R.style.DarkTheme

    var openFile: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppBus.reg(this)
    }

    override fun onDestroy() {
        AppBus.unreg(this)
        super.onDestroy()
    }

    abstract val testFiles: Array<String>
    abstract val assetFolder: String

    abstract val scriptType: String

    inner class MyPrinter : OnPrint {
        override fun onPrint(l: Int, output: String?) {
            val pair = when (l) {
                OnPrint.ERROR -> Pair(R.color.red_500, "ERROR: ")
                OnPrint.WARN -> Pair(R.color.orange_700, "WARN: ")
                OnPrint.INFO -> Pair(R.color.orange_700, "INFO: ")
                else -> Pair(null, "")
            }

            val (i: CharSequence, v: CharSequence) = if (pair.first == null) {
                (pair.second to output) as Pair<CharSequence, CharSequence>
            } else {
                (pair.second.spanColor(color(pair.first!!)) to (output
                    ?: "").spanColor(pair.first!!))
            }
            synchronized(logList) {
                logList.add(i)
                logList.add(v)
                logDialog?.append(i)
                logDialog?.append(v)
            }
        }
    }

    var print: OnPrint = MyPrinter()

    private var runArgs: String? = null

    var loaded = false
    override fun onStart() {
        super.onStart()
        if (!loaded) {
            if (testFiles.isNotEmpty()) {
                setCode(LuaUtil.getTextFromAsset(this, assetFolder + testFiles[0]))
            }
            initView()
            initEditorToolbar()
            loaded = true
        }
    }

    private fun hideInputMethod() {
        val mInputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mInputManager.hideSoftInputFromWindow(findViewById<View>(R.id.root).windowToken, 0)
    }

    override fun initView() {
        toggle_functions.setOnClickListener {
            if (functions_grid.visibility == View.GONE) {
                functions_grid.visibility = View.VISIBLE
                functions_grid.animate().setDuration(1000).alpha(1f).alphaBy(0f).start()
                activityRootView.viewTreeObserver.removeOnGlobalLayoutListener(gloLis)
                hideInputMethod()
                Handler().postDelayed({
                    activityRootView.viewTreeObserver.addOnGlobalLayoutListener(gloLis)
                }, 100)
            } else {
                functions_grid.visibility = View.GONE
                functions_grid.animate().setDuration(500).alpha(0f).alphaBy(1f).start()
            }
            functions_grid.animate().setDuration(1000).start()
        }
        EditorFunsHelper(this, supportFragmentManager, func_pager, tab_lay) {
            codeEditor.insert(it)
        }
        activityRootView.viewTreeObserver.addOnGlobalLayoutListener(gloLis)
    }

    private val gloLis = ViewTreeObserver.OnGlobalLayoutListener {
        val heightDiff = activityRootView.rootView.height - activityRootView.height
        if (heightDiff > 500) {
            functions_grid.visibility = View.GONE
        }
    }
    private val activityRootView: View by lazy { findViewById<View>(R.id.root) }

    private fun initEditorToolbar() {
        symbol_line.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
        symbol_line.adapter = SymbolsAdapter(this, codeEditor, symbols)
    }

    abstract val symbols: List<Symbol>

    private val haveFileUnsaved: Boolean
        get() = openFile != null && codeEditor.isEdit

    override fun onBackPressed() {
        if (haveFileUnsaved) {
            MaterialDialog(this).show {
                title(text = "提示")
                message(text = "有未保存的文件，是否放弃保存")
                positiveButton {
                    finish()
                }
                negativeButton()
                neutralButton(text = "保存退出") {
                    codeEditor.saveFile(openFile!!)
                    finish()
                }
            }
            return
        } else super.onBackPressed()
    }

    private val logList = mutableListOf<CharSequence>()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_code_run -> {
                clearLog()
                val ac = Action(editorText, scriptType)
                try {
                    ac.param = parseSimpleMap(runArgs)
                    AppBus.post(ac)
                } catch (e: Exception) {
                    GlobalApp.toastError("参数设置错误")
                }
            }
            R.id.menu_vosp -> {
                SystemBridge.openUrl("https://github.com/Vove7/VOSP")
            }
            R.id.menu_view_api -> {
                SystemBridge.openUrl("https://vove.gitee.io/2019/11/02/View-Api/")
            }
            R.id.menu_api_doc -> {
                SystemBridge.openUrl("https://vove.gitee.io/2018/09/25/Script-Api/")
            }
            R.id.menu_set_arg -> {
                MaterialDialog(this)
                        .title(text = "设置参数")
                        .input(prefill = runArgs, hint = "例如：n : 1234, k : sss") { _, charSequence ->
                            runArgs = charSequence.toString()
                            GlobalApp.toastInfo("参数已设置")
                        }.positiveButton()
                        .negativeButton(text = "清空") {
                            runArgs = null
                        }
                        .show()
            }

            R.id.menu_code_stop -> {
                AppBus.post(AppBus.ACTION_STOP_EXEC)
            }
            R.id.menu_sel_code -> {
                AlertDialog.Builder(this).setTitle(R.string.text_select_script).setItems(testFiles) { d, p ->
                    openFile = null
                    setCode(LuaUtil.getTextFromAsset(this@CodeEditorActivity,
                            assetFolder + testFiles[p]))
                    d.dismiss()
                }.show()
            }
            R.id.menu_goto_line -> {
                codeEditor.gotoLine()
            }
            R.id.menu_undo -> {
                codeEditor.undo()
            }
            R.id.menu_redo -> codeEditor.redo()
            R.id.menu_find -> {
                codeEditor.find()
            }
            R.id.menu_switch_ui_mode -> {
//                dark = !dark
//                codeEditor.setDark(dark)
            }
            R.id.menu_code_format -> codeEditor.format()
            R.id.menu_save_file -> {
                if (openFile != null) {
                    codeEditor.saveFile(openFile!!)
                } else {
                    GlobalApp.toastError("示例文件无法保存")
                }
            }
            R.id.menu_log -> showLog()
            R.id.menu_open_file -> {
                if (haveFileUnsaved) {
                    AlertDialog.Builder(this).setTitle("提示")
                            .setMessage("有未保存的文件，是否放弃保存")
                            .setPositiveButton(R.string.text_ok) { _, _ ->
                                openFile()
                            }.setNegativeButton(R.string.text_cancel) { _, _ ->
                            }.setNegativeButton("保存") { _, _ ->
                                codeEditor.saveFile(openFile!!)
                                openFile()
                            }
                            .show()
                    return true
                } else openFile()
            }
            else -> {
            }
        }

        return super.onOptionsItemSelected(item)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onExecutorStatus(es: ExecutorStatus) {
        when (es.what) {
            ON_EXECUTE_FINISHED -> showLog()
        }
    }

    var logDialog: ProgressTextDialog? = null
    private fun showLog() {
        logDialog = ProgressTextDialog(this, "输出", true, autoScroll = true)
                .neutralButton(text = "清空") {
                    clearLog()
                    showLog()
                }
                .positiveButton()
                .onDismiss {
                    logDialog = null
                } as ProgressTextDialog
        synchronized(logList) {
            logList.forEach {
                logDialog?.append(it)
            }
        }
    }


    private fun openFile() {
        val selIntent = Intent(Intent.ACTION_GET_CONTENT)
        selIntent.type = "*/*"
        selIntent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(selIntent, 1)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            GlobalApp.toastError(getString(R.string.text_cannot_open_file_manager))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {//选择文件回调
            when (requestCode) {
                1 -> {
                    val uri = data?.data
                    if (uri != null) {
                        try {
                            val path = UriUtils.getPathFromUri(this, uri)
                            codeEditor.openFile(path!!)
                            codeEditor.isEdit = false
                            openFile = path
                        } catch (e: Exception) {
                            GlobalLog.err(e)
                            GlobalApp.toastError(getString(R.string.text_open_failed))
                        }
                    } else {
                        GlobalApp.toastError(getString(R.string.text_open_failed))
                    }
                }
                else -> {
                }
            }
        }
    }

//    var dark: Boolean = false
//        get() = SpHelper(this).getBoolean("editor_dark", false)
//        set(value) {
//            SpHelper(this).set("editor_dark", value)
//            field = value
//        }

    private fun clearLog() {
        logList.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_code_editor, menu)
        return true
    }

    private fun setCode(s: String) {
        codeEditor.setText(s)
    }

    private val editorText: String
        get() = codeEditor.getEditorContent() ?: ""

    class SymbolsAdapter(val c: Context, val editor: CodeEditorOperation, val symbols: List<Symbol>)
        : androidx.recyclerview.widget.RecyclerView.Adapter<V>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): V {
            val v = LayoutInflater.from(c).inflate(R.layout.item_of_symbols, null)
            return V(v)
        }

        override fun getItemCount(): Int = symbols.size


        override fun onBindViewHolder(p0: V, p1: Int) {
            (p0.textView as TextView).apply {
                text = symbols[p1].show
                setOnClickListener {
                    editor.insert(symbols[p1].fillText)
                }
            }
        }
    }

    class V(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
        val textView = v
    }

    companion object {
        private val commonSymbols
            get() = listOf(Symbol("("),
                    Symbol(")"), Symbol("<"),
                    Symbol(">"), Symbol("{"),
                    Symbol("}"), Symbol("["), Symbol("]"),
                    Symbol("\""), Symbol("'"), Symbol("/"),
                    Symbol("|"), Symbol("+"),
                    Symbol("-"), Symbol("\\"), Symbol("*"), Symbol("?"),
                    Symbol("&"), Symbol("="), Symbol("%"), Symbol("")
            )

        val jsSymbols
            get() = mutableListOf(
                    Symbol("🚩", "requireAccessibility()\n"),
                    Symbol("⇥", "  "),
                    Symbol("fun", "function () {\n  \n}\n"),
                    Symbol("!")
            ).also { it.addAll(commonSymbols) }
        val luaSymbols
            get() = mutableListOf(
                    Symbol("🚩", "requireAccessibility()\n"),
                    Symbol("⇥", "  "),
                    Symbol("fun", "function ()\n  \nend\n"),
                    Symbol("not"), Symbol("end"), Symbol("then"),
                    Symbol("#")
            ).also { it.addAll(commonSymbols) }

        @Throws
        fun parseSimpleMap(s: String?): Map<String, Any> {
            s ?: return hashMapOf()
            val ss = s.trim()

            return ss.split(',').let {
                mutableMapOf<String, Any>().apply {
                    it.forEach {
                        val sss = it.split(":")
                        put(sss[0].trim(), sss[1].trim())
                    }
                }
            }
        }
    }

    class Symbol(
            val show: String,
            val fillText: String = show
    )

}
