package cn.vove7.accessibilityservicedemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import cn.vove7.accessibilityservicedemo.utils.Bus
import cn.vove7.accessibilityservicedemo.utils.LogMessage
import cn.vove7.accessibilityservicedemo.utils.SpeechAction
import cn.vove7.accessibilityservicedemo.utils.SpeechAction.Companion.ACTION_START
import cn.vove7.accessibilityservicedemo.utils.SpeechAction.Companion.ACTION_STOP
import cn.vove7.accessibilityservicedemo.utils.VoiceData
import cn.vove7.vtp.runtimepermission.PermissionUtils
import cn.vove7.vtp.toast.VToast
import kotlinx.android.synthetic.main.activity_voice.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class VoiceTestActivity : AppCompatActivity() {

    private val requirePermission = arrayOf(
            "android.permission.RECORD_AUDIO",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.INTERNET",
            "android.permission.READ_PHONE_STATE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    )
    private val mustPermission = arrayOf(
            "android.permission.RECORD_AUDIO",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.INTERNET",
            "android.permission.READ_PHONE_STATE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    )
    lateinit var logText: TextView
    lateinit var scr: ScrollView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice)
        logText = findViewById(R.id.textView3)
        scr = findViewById(R.id.scrollView)
        PermissionUtils.autoRequestPermission(this, requirePermission, 9)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (!PermissionUtils.isAllGranted(this, mustPermission)) {
            VToast.with(this).bottom().showShort("无权限")
            finish()
        }
    }

    override fun onResume() {
        Bus.reg(this)
        super.onResume()
    }

    override fun onStop() {
        Bus.unreg(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun log(log: LogMessage) {
        logText.append(log.msg + "\n")
        scr.fullScroll(View.FOCUS_DOWN)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showResult(data: VoiceData) {
        when (data.what) {
            VoiceData.WHAT_TEMP -> {
                result_text.text = data.tempResult
            }
            VoiceData.WHAT_VOL -> {
                volume_per.progress = data.volumePercent
            }
        }
    }

    fun stop(v: View) {
        Bus.postSpeechAction(SpeechAction(ACTION_STOP))
    }

    fun start(v: View) {
        Bus.postSpeechAction(SpeechAction(ACTION_START))
    }


}
