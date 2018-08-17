package cn.vove7.jarvis.activities

import android.animation.Animator
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import cn.vove7.appbus.*
import cn.vove7.jarvis.R
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_voice)
        logText = findViewById(R.id.log)
        PermissionUtils.autoRequestPermission(this, requirePermission, 9)
        voice_bkg.visibility = View.GONE
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
    }

    var op = 0
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showResult(data: VoiceData) {
        when (data.what) {
            WHAT_VOICE_TEMP -> {
                result_text.text = data.data
                logText.append(data.data + "\n")
            }
            WHAT_VOICE_VOL -> {
                volume_per.progress = data.volumePercent
                updateCircle(op, data.volumePercent)
                op = data.volumePercent
            }
            WHAT_VOICE_ERR -> {
                logText.append("识别失败\n")
            }
        }
    }

    fun stop(v: View) {
        AppBus.postSpeechRecoAction(SpeechRecoAction.ActionCode.ACTION_STOP_RECO)
    }

    fun start(v: View) {
        AppBus.postSpeechRecoAction(SpeechRecoAction.ActionCode.ACTION_START_RECO)
    }

    var c: Animator? = null
    private fun updateCircle(op: Int, np: Int) {
        val oldR = (op.toFloat() / 100) * voice_bkg.width / 2
        val newR = (np.toFloat() / 100) * voice_bkg.width / 2

        voice_bkg.visibility = View.VISIBLE
        c?.cancel()
        c = ViewAnimationUtils.createCircularReveal(
                voice_bkg, voice_bkg.width / 2,
                voice_bkg.height / 2, oldR, newR
        )
        c!!.duration = 200
        c!!.interpolator = AccelerateDecelerateInterpolator()
        c!!.start()
    }
}
