package cn.vove7.jarvis.view.floatwindows

import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.SpeechAction
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.utils.runOnUi
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
import kotlinx.android.synthetic.main.toast_listening_text.view.*

/**
 * # FloatyPanel
 * 悬浮语音面板
 * @author 17719247306
 * 2018/9/9
 */
class FloatyPanel : AbFloatWindow(GlobalApp.APP) {
    override var posY: Int = 0

    //    lateinit var rootView: LinearLayout
    override fun layoutResId(): Int = R.layout.toast_listening_text

    override val onNoPermission: () -> Unit = {
        AppBus.postSpeechAction(SpeechAction.ActionCode.ACTION_CANCEL_RECOG)
        AppBus.post(RequestPermission("悬浮窗权限"))
    }

    private var ani = 0
    /**
     * TODO 切换动画
     * @param resId Int
     */
    private fun setAniRes(resId: Int) {
        if (ani == resId) return
        ani = resId
        contentView?.ani_img?.apply {
            post {
                setImageResource(resId)
                (drawable as? AnimationDrawable)?.start()
            }
        }
    }

    fun showParseAni() {
        runOnUi {
            setAniRes(R.drawable.parsing_animation)
            if (!isShowing)
                show()
        }
    }

    override fun onCreateView(view: View) {
        view.body.setPadding(0, statusbarHeight + 30, 0, 30)
    }

    fun show(text: String) {
        removeDelayHide()
        runOnUi {
            show()
            setAniRes(R.drawable.listening_animation)
            voiceText = text
            contentView?.voice_text?.text = text
        }
    }

    var voiceText = ""

    override fun afterShow() {
        contentView?.voice_text?.text = voiceText

        contentView?.body?.also {
            it.startAnimation(AnimationUtils.loadAnimation(context,
                    R.anim.pop_fade_in))
        }

    }

    override val exitAni: Int? = R.anim.pop_fade_out

    override fun onRemove() {
        ani = 0
    }

    private fun removeDelayHide() {
        delayHandler?.removeCallbacks(delayHide)
    }

    fun showAndHideDelay(text: String) {
        show(text)
        hideDelay(1000)
    }

    var delayHide = Runnable {
        hide()
    }

    var delayHandler: Handler? = null
    fun hideDelay(delay: Long = 800) {
        runOnUi {
            if (delayHandler == null) delayHandler = Handler()
            delayHandler?.postDelayed(delayHide, delay)
        }
        Vog.d("hide delay $delay")
    }

    fun hideImmediately() {//立即
        runOnUi {
            hide()
        }
    }

}