package cn.vove7.jarvis

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import cn.vove7.appbus.AppBus
import cn.vove7.appbus.BaseAction
import cn.vove7.appbus.LogMessage
import cn.vove7.appbus.VoiceData
import cn.vove7.jarvis.services.MainService.Companion.WHAT_VOICE_ERR
import cn.vove7.jarvis.services.MainService.Companion.WHAT_VOICE_TEMP
import cn.vove7.jarvis.services.MainService.Companion.WHAT_VOICE_VOL
import cn.vove7.vtp.runtimepermission.PermissionUtils
import cn.vove7.vtp.toast.Voast
import kotlinx.android.synthetic.main.activity_voice.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class VoiceTestActivity : Activity() {


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
            Voast.with(this).bottom().showShort("无权限")
            finish()
        }
    }

    override fun onResume() {
        AppBus.reg(this)
        super.onResume()
    }

    override fun onStop() {
        AppBus.unreg(this)
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
            WHAT_VOICE_TEMP -> {
                result_text.text = data.tempResult
                logText.append(data.tempResult + "\n")
            }
            WHAT_VOICE_VOL -> {
                volume_per.progress = data.volumePercent
            }
            WHAT_VOICE_ERR -> {
                logText.append("识别出错")
            }
        }
    }

    fun stop(v: View) {
        AppBus.postSpeechRecoAction(BaseAction.ACTION_STOP)
    }

    fun start(v: View) {
        AppBus.postSpeechRecoAction(BaseAction.ACTION_START)
    }
}
