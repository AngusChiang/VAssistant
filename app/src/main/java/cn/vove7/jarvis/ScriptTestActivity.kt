package cn.vove7.jarvis

import android.app.Activity
import android.os.Bundle
import android.view.View
import cn.vove7.appbus.AppBus
import cn.vove7.datamanager.parse.model.Action
import cn.vove7.parseengine.engine.ParseEngine
import cn.vove7.vtp.asset.AssetHelper
import cn.vove7.vtp.toast.Voast
import kotlinx.android.synthetic.main.activity_script_test.*

class ScriptTestActivity : Activity() {

    private val files = arrayOf(
            "s/alipay_ss.txt"
            , "s/ali_ss_s.txt"
            , "s/qq_ss.txt"
            , "s/voice_text.txt"
            , "s/scroll_test.txt"
            , "s/test.txt"
            , "s/qq_send.txt"
            , "s/qq_send_s.txt"
    )
    var now = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_script_test)
        script_text.setText(AssetHelper.getStrFromAsset(this, files[now]))
    }

    fun last(view: View) {
        now = (--now + files.size) % files.size
        script_text.setText(AssetHelper.getStrFromAsset(this, files[now]))
    }

    fun next(view: View) {
        now = (++now) % files.size
        script_text.setText(AssetHelper.getStrFromAsset(this, files[now]))
    }

    fun runScript(view: View) {
        val toast = Voast.with(this, true).top()
        when (view.id) {
            R.id.button3 -> {
                toast.showShort("开始解析")
                val parseResult = ParseEngine.parseGlobalAction(script_text.text.toString(), "")
                if (parseResult.isSuccess) {
                    toast.showShort("解析成功")
                    AppBus.post(parseResult.actionQueue)
                } else {
                    toast.showShort("解析失败")
                }
            }
            R.id.button11 -> {
                val ac = Action(script_text.text.toString())
                AppBus.post(ac)
            }
        }

    }

    fun stopScript(view: View) {
        AppBus.post("stop execQueue")
    }
}