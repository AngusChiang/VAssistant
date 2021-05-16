package cn.vove7.jarvis.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.utils.gone
import cn.vove7.jarvis.R
import cn.vove7.jarvis.databinding.ActivityVoiceBinding
import cn.vove7.jarvis.speech.VoiceData
import cn.vove7.jarvis.speech.SpeechConst.Companion.CODE_VOICE_ERR
import cn.vove7.jarvis.speech.SpeechConst.Companion.CODE_VOICE_TEMP
import cn.vove7.jarvis.speech.SpeechConst.Companion.CODE_VOICE_VOL
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.runtimepermission.PermissionUtils
import cn.vove7.vtp.toast.Voast
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

    val viewBinding by lazy {
        ActivityVoiceBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(viewBinding.root)
        PermissionUtils.autoRequestPermission(this, requirePermission, 9)
        viewBinding.voiceBkg.gone()
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


    var op = 0
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showResult(data: VoiceData) {
        when (data.what) {
            CODE_VOICE_TEMP -> {
                findViewById<TextView>(R.id.result_text).text = data.data
                viewBinding.log.append(data.data + "\n")
            }
            CODE_VOICE_VOL -> {
                handle.sendMessage(handle.obtainMessage(0, data.volumePercent, 0))
            }
            CODE_VOICE_ERR -> {
                viewBinding.log.append("识别失败\n")
            }
        }
    }

    private val handle = Handler {
        viewBinding.volumePer.progress = it.arg1
        updateCircle(op, it.arg1)
        return@Handler true
    }

    fun stop(v: View) {
        AppBus.post(AppBus.ACTION_STOP_RECOG)
    }

    fun start(v: View) {
        AppBus.post(AppBus.ACTION_START_RECOG)
    }

    var c: Animator? = null
    var end = true
    private fun updateCircle(op: Int, np: Int) {
        if (!end) {
            return
        }
        end = false
        val oldR = (op.toFloat() / 100) * viewBinding.voiceBkg.width / 2
        val newR = (np.toFloat() / 100) * viewBinding.voiceBkg.width / 2
        Vog.d("$oldR -> $newR")
        viewBinding.voiceBkg.visibility = View.VISIBLE
//        c?.cancel()
        c = ViewAnimationUtils.createCircularReveal(
                viewBinding.voiceBkg, viewBinding.voiceBkg.width / 2,
                viewBinding.voiceBkg.height / 2, oldR, newR
        )
        c!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                end = true
                super.onAnimationEnd(animation)
            }
        })

        c!!.duration = 200
        c!!.interpolator = AccelerateDecelerateInterpolator()
        c!!.start()

        this.op = np
    }
}
