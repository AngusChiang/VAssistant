package cn.vove7.accessibilityservicedemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ScrollView
import cn.vove7.accessibilityservicedemo.utils.MessageEvent
import kotlinx.android.synthetic.main.activity_script_test.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ScriptTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_script_test)

    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun append(message: MessageEvent) {
        output_text.append(message.msg + "\n")
        scrollView2.fullScroll(ScrollView.FOCUS_DOWN)
    }

    fun runScript(view: View) {

    }

    fun stopScript(view: View) {

    }
}