package cn.vove7.accessibilityservicedemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ScrollView
import cn.vove7.accessibilityservicedemo.services.MyAccessibilityService
import cn.vove7.accessibilityservicedemo.utils.Bus
import cn.vove7.accessibilityservicedemo.utils.MessageEvent
import cn.vove7.accessibilityservicedemo.utils.SpeechAction
import cn.vove7.executorengine.GetAccessibilityBridge
import cn.vove7.executorengine.bridge.AccessibilityBridge
import cn.vove7.parseengine.engine.ParseEngine
import cn.vove7.vtp.toast.Voast
import kotlinx.android.synthetic.main.activity_script_test.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ScriptTestActivity : AppCompatActivity(), GetAccessibilityBridge {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_script_test)
    }

    override fun getBridge(): AccessibilityBridge? {
        return MyAccessibilityService.accessibilityService
    }

    override fun onResume() {
        super.onResume()
        Bus.reg(this)
    }

    override fun onStop() {
        Bus.unreg(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun append(message: MessageEvent) {
        output_text.append(message.msg + "\n")
        scrollView2.fullScroll(ScrollView.FOCUS_DOWN)
    }

    fun runScript(view: View) {
        val toast = Voast.with(this, true).top()
        toast.showShort("开始解析")
        val parseResult = ParseEngine.parseAction(script_text.text.toString())
        if (parseResult.isSuccess) {
            toast.showShort("解析成功")
            Bus.post(parseResult.actionQueue)
        } else {
            toast.showShort("解析失败")
        }
    }

    fun stopScript(view: View) {
        Bus.post("stop exec")
    }
}