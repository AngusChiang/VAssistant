package cn.vove7.jarvis.view.floatwindows

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import cn.vove7.jarvis.R
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.speech.services.SpeechService
import cn.vove7.appbus.AppBus
import cn.vove7.appbus.SpeechAction
import cn.vove7.appbus.VoiceData
import cn.vove7.vtp.floatwindow.AbFloatWindow
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 语音悬浮窗
 *
 * Created by Vove on 2018/7/1
 */
class VoiceFloat(
        context: Context,
        posX: Int = 0,
        posY: Int = 0,
        mParams: WindowManager.LayoutParams? = null
) : AbFloatWindow<VoiceFloat.Holder>(context, mParams, posX, posY) {
    init {
    }

    override fun layoutResId(): Int = R.layout.float_voice

    override fun onCreateViewHolder(view: View): Holder {
        val holder = Holder(view)
        holder.voiceImage.setOnClickListener {
            if (SpeechService.instance.isListening()) {
                AppBus.postSpeechAction(SpeechAction(SpeechAction.ACTION_STOP))
            } else
                AppBus.postSpeechAction(SpeechAction(SpeechAction.ACTION_START))
        }

        return holder
    }


    override fun show() {
        AppBus.reg(this)
        super.show()
    }

    override fun hide() {
        AppBus.unreg(this)
        super.hide()
    }

    class Holder(view: View) : AbFloatWindow.ViewHolder(view) {
        val voiceImage = view.findViewById<ImageButton>(R.id.voice)!!
        val result = view.findViewById<TextView>(R.id.result)!!

    }
}

@Subscribe(threadMode = ThreadMode.MAIN)
fun VoiceFloat.showResult(data: VoiceData) {
    when (data.what) {
        MainService.WHAT_VOICE_TEMP -> {
            holder.result.text = data.tempResult
        }
        MainService.WHAT_VOICE_VOL -> {

        }
        MainService.WHAT_VOICE_ERR -> {
            holder.result.text = "识别出错"
        }
    }
}