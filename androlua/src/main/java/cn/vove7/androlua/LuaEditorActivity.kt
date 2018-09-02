package cn.vove7.androlua

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import cn.vove7.androlua.luabridge.LuaUtil
import cn.vove7.androlua.luautils.LuaEditor
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionParam
import cn.vove7.common.executor.OnPrint
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.view.span.ColourTextClickableSpan
import java.io.IOException
import java.util.*

class LuaEditorActivity : Activity(), OnClickListener {

    lateinit var execute: Button

    lateinit var source: LuaEditor
    lateinit var status: TextView
    lateinit var luaArgs: EditText

    var nowIndex = 0

    var testFiles = arrayOfNulls<String>(0)
    var scripts = ArrayList<String>()

    @SuppressLint("HandlerLeak")
    internal var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val pair = when (msg.what) {
                OnPrint.ERROR -> {
                    Pair(cn.vove7.vtp.R.color.red_500, "ERROR: ")
                }
                OnPrint.WARN -> {
                    Pair(cn.vove7.vtp.R.color.orange_700, "WARN: ")
                }
                OnPrint.INFO -> {
                    Pair(cn.vove7.vtp.R.color.orange_700, "INFO: ")
                }
                else -> {
                    Pair(cn.vove7.vtp.R.color.default_text_color, "")
                }
            }
            val i = ColourTextClickableSpan(this@LuaEditorActivity, pair.second, colorId = pair.first, listener = null)
            status.append(i.spanStr)

            val v = ColourTextClickableSpan(this@LuaEditorActivity, msg.data.getString("data"), colorId = pair.first, listener = null)
            status.append(v.spanStr)
            if (!status.isFocused) {
                val offset = status.lineCount * status.lineHeight
                if (offset > status.height) {
                    status.scrollTo(0, offset - status.height)
                }
            }
        }
    }
    internal var print: OnPrint = object : OnPrint {
        override fun onPrint(l: Int, output: String) {
            val m = Message()
            val b = Bundle()
            b.putString("data", output)
            m.what = l
            m.data = b
            handler.sendMessage(m)
        }
    }

    //TODO ??
    override fun onBackPressed() {
        super.onBackPressed()
    }

    @SuppressLint("HandlerLeak")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lua_editor)

        execute = findViewById(R.id.executeBtn)
        execute.setOnClickListener(this)

        source = findViewById(R.id.source)
        luaArgs = findViewById(R.id.lua_args)

        status = findViewById(R.id.statusText)
        status.movementMethod = ScrollingMovementMethod.getInstance()

//        luaHelper = LuaApp.APP.luaHelper

        try {
            testFiles = assets.list("lua_sample")
            testFiles.forEach {
                scripts.add(it!!)
            }
            Vog.d(this, "onCreate  ----> " + Arrays.toString(testFiles))
            if (testFiles.isNotEmpty()) {
                source.setText(LuaUtil.getTextFromAsset(this, "lua_sample/" + testFiles[0]))
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun onResume() {
        super.onResume()
        LuaHelper.regPrint(print)
        //serverThread = new RemoteDebugServer(this, luaHelper, handler);
        //serverThread.start();
    }

    override fun onStop() {
        super.onStop()
        LuaHelper.unRegPrint(print)
        //serverThread.stopped = true;
    }

    override fun onClick(view: View) {
        val len = testFiles.size
        val i = view.id
        when (i) {
            R.id.executeBtn -> {
                val src = source.text.toString()
                status.text = ""
                status.scrollTo(0, 0)
                val ac = Action(src, Action.SCRIPT_TYPE_LUA)
                ac.param = ActionParam()
                ac.param.value = luaArgs.text.toString()
                AppBus.post(ac)
            }
            R.id.r -> {
                nowIndex = ++nowIndex % len
                source.setText(LuaUtil.getTextFromAsset(this, "lua_sample/" + testFiles[nowIndex]))
            }
            R.id.l -> {
                nowIndex = (--nowIndex + len) % len
                source.setText(LuaUtil.getTextFromAsset(this, "lua_sample/" + testFiles[nowIndex]))
            }
            R.id.stop -> AppBus.post("stop_execQueue")
            R.id.choose_script -> {//选择jio本
                AlertDialog.Builder(this).setTitle(R.string.text_select_script).setItems(testFiles) { d, p ->
                    nowIndex = p
                    source.setText(LuaUtil.getTextFromAsset(this@LuaEditorActivity,
                            "lua_sample/" + testFiles[p]))
                    d.dismiss()
                }.show()
            }
        }
    }

}