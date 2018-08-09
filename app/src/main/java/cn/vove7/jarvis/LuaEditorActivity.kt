package cn.vove7.jarvis

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
import cn.vove7.androlua.LuaHelper
import cn.vove7.androlua.R
import cn.vove7.androlua.luabridge.LuaUtil
import cn.vove7.androlua.luautils.LuaEditor
import cn.vove7.androlua.luautils.LuaManagerI
import cn.vove7.androlua.luautils.LuaPrinter
import cn.vove7.appbus.AppBus
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.datamanager.parse.model.Param
import cn.vove7.vtp.log.Vog
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
            when (msg.what) {
                LuaManagerI.E -> status.append("E: ")
                LuaManagerI.W -> status.append("W: ")
            }
            status.append(msg.data.getString("data"))
            if (!status.isFocused) {
                val offset = status.lineCount * status.lineHeight
                if (offset > status.height) {
                    status.scrollTo(0, offset - status.height)
                }
            }
        }
    }
    internal var print: LuaPrinter.OnPrint = object : LuaPrinter.OnPrint {
        override fun onPrint(l: Int, output: String) {
            val m = Message()
            val b = Bundle()
            b.putString("data", output)
            m.what = l
            m.data = b
            handler.sendMessage(m)
        }
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
            testFiles = assets.list("sample")
            testFiles.forEach {
                scripts.add(it!!)
            }
            Vog.d(this, "onCreate  ----> " + Arrays.toString(testFiles))
            if (testFiles.isNotEmpty()) {
                source.setText(LuaUtil.getTextFromAsset(this, "sample/" + testFiles[0]))
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
                val ac = Action(src)
                ac.param = Param()
                ac.param.value = luaArgs.text.toString()
                AppBus.post(ac)
            }
            R.id.r -> {
                nowIndex = ++nowIndex % len
                source.setText(LuaUtil.getTextFromAsset(this, "sample/" + testFiles[nowIndex]))
            }
            R.id.l -> {
                nowIndex = (--nowIndex + len) % len
                source.setText(LuaUtil.getTextFromAsset(this, "sample/" + testFiles[nowIndex]))
            }
            R.id.stop -> AppBus.post("stop execQueue")
            R.id.choose_script -> {//选择jio本
                AlertDialog.Builder(this).setTitle("选择jio本").setItems(testFiles) { d, p ->
                    nowIndex = p
                    source.setText(LuaUtil.getTextFromAsset(this@LuaEditorActivity,
                            "sample/" + testFiles[p]))
                    d.dismiss()
                }.show()
            }
        }
    }

}