package cn.vove7.jarvis.view.floatwindows

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.model.RequestPermission
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.utils.startActivity
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.ResultPickerActivity
import cn.vove7.jarvis.chat.UrlItem
import cn.vove7.vtp.builder.BundleBuilder
import cn.vove7.vtp.log.Vog

/**
 * # FloatyPanel
 * 悬浮语音面板
 * @author 17719247306
 * 2018/9/9
 */
abstract class FloatyPanel(width: Int, height: Int) : AbFloatWindow(
        GlobalApp.APP, width, height
) {

    internal val screenWidth = context.resources.displayMetrics.widthPixels

    override fun onCreateView(view: View) {
        animationBody.setPadding(10, statusbarHeight + animationBody.paddingTop, 10, 15)
    }

    override val onNoPermission: () -> Unit = {
        AppBus.post(AppBus.ACTION_CANCEL_RECOG)
        AppBus.post(RequestPermission("悬浮窗权限"))
    }

    final override fun show(text: String?) {
        Vog.d("显示：$text")
        removeDelayHide()
        runOnUi {
            if (isHiding) superRemove()
            show()
            showListeningAni()
            voiceText = text
            showText(text)
        }
    }

    override fun showUserWord(text: String?) {
        show(text)
    }

    override fun showTextResult(result: String) {
        show(result)
    }

    override fun showListResult(tite: String, items: List<UrlItem>) {
        when {
            items.size == 1 -> SystemBridge.openUrl(items[0].url)
            else -> {
                GlobalApp.APP.startActivity<ResultPickerActivity> {
                    putExtra("title", tite)
                    putExtra("data", BundleBuilder().put("items", items).data)
                }
            }
        }

    }

    private fun showText(text: String?) {
        contentView?.findViewById<TextView>(R.id.voice_text)?.text = text
    }


    private var voiceText: String? = ""

    override fun afterShow() {
        showEnterAnimation()
        showText(voiceText)
    }

    private var isHiding = false

    override fun onRemove() {
        isHiding = true
        showExitAnimation()
    }

    /**
     * 执行superRemove移除视图
     */
    abstract fun showExitAnimation()
    abstract fun showEnterAnimation()

    internal fun superRemove() {
        isHiding = false
        super.onRemove()
    }


    private fun removeDelayHide() {
        hideInterrupt = true
        delayHandler.removeCallbacks(delayHide)
    }

    private var hideInterrupt = false

    private var delayHide = Runnable {
        if (hideInterrupt) {
            Vog.d("打断 ")
            return@Runnable
        } else {
            Vog.d("delayHide执行")
            hide()
        }
    }

    private val delayHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun hideDelay(delay: Long) {
        hideInterrupt = false
        delayHandler.postDelayed(delayHide, delay)
        Vog.d("hide delay $delay")
    }

}