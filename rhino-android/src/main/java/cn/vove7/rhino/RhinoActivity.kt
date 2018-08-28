package cn.vove7.rhino

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionParam
import cn.vove7.common.executor.OnPrint
import cn.vove7.rhino.api.RhinoApi
import cn.vove7.vtp.asset.AssetHelper
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.view.span.ColourTextClickableSpan
import kotlinx.android.synthetic.main.activity_rhino.*
import java.io.IOException
import java.util.*

class RhinoActivity : AppCompatActivity() {

    lateinit var logText: TextView
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
            val i = ColourTextClickableSpan(this@RhinoActivity, pair.second, colorId = pair.first, listener = null)
            logText.append(i.spanStr)

            val v = ColourTextClickableSpan(this@RhinoActivity, msg.data.getString("data"), colorId = pair.first, listener = null)
            logText.append(v.spanStr)
            if (!logText.isFocused) {
                val offset = logText.lineCount * logText.lineHeight
                if (offset > logText.height) {
                    logText.scrollTo(0, offset - logText.height)
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

    override fun onResume() {
        RhinoApi.regPrint(print)
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rhino)
        Vog.d(this, "main:" + Thread.currentThread().toString())
        logText = findViewById(R.id.statusText)

        exec_js.setOnClickListener {

            val src = js_editText.text.toString()
            logText.text = ""
            logText.scrollTo(0, 0)
            val ac = Action(src, Action.SCRIPT_TYPE_JS)
            ac.param = ActionParam()
            ac.param.value = js_args.text.toString()
            AppBus.post(ac)
        }
        stop_js.setOnClickListener {
            AppBus.post("stop execQueue")
        }

        try {
            testFiles = assets.list("test&sample")
            testFiles.forEach {
                scripts.add(it!!)
            }
            Vog.d(this, "onCreate  ----> " + Arrays.toString(testFiles))
            if (testFiles.isNotEmpty()) {
                js_editText.setText(AssetHelper.getStrFromAsset(this, "test&sample/" + testFiles[0]))
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    var nowIndex = 0

    override fun onStop() {
        RhinoApi.unregPrint(print)
        super.onStop()
    }

    fun onClick(v: View) {
        val len = testFiles.size
        when (v.id) {

            R.id.r -> {
                nowIndex = ++nowIndex % len
                js_editText.setText(AssetHelper.getStrFromAsset(this, "test&sample/" + testFiles[nowIndex]))
            }
            R.id.l -> {
                nowIndex = (--nowIndex + len) % len
                js_editText.setText(AssetHelper.getStrFromAsset(this, "test&sample/" + testFiles[nowIndex]))
            }
            R.id.choose_script -> {//选择jio本
                AlertDialog.Builder(this).setTitle(R.string.text_select_script).setItems(testFiles) { d, p ->
                    nowIndex = p
                    js_editText.setText(AssetHelper.getStrFromAsset(this, "test&sample/" + testFiles[p]))
                    d.dismiss()
                }.show()
            }

        }

    }
}
