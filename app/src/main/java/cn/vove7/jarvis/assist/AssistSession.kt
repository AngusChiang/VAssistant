package cn.vove7.jarvis.assist

import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.support.annotation.RequiresApi
import android.view.KeyEvent
import android.view.View
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.AppBus.EVENT_BEGIN_RECO
import cn.vove7.common.appbus.AppBus.EVENT_ERROR_RECO
import cn.vove7.common.appbus.AppBus.EVENT_FINISH_RECO
import cn.vove7.common.appbus.AppBus.EVENT_HIDE_FLOAT

import cn.vove7.common.bridges.UtilBridge
import cn.vove7.jarvis.services.MainService
import cn.vove7.jarvis.R
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.vtp.log.Vog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.concurrent.thread

/**
 * # AssistSession
 * 会话界面
 */
@RequiresApi(api = Build.VERSION_CODES.M)
class AssistSession(context: Context) : VoiceInteractionSession(context) {

    var screenshot: Bitmap? = null
    override fun onAssistStructureFailure(failure: Throwable) {
        failure.printStackTrace()
        Vog.d(this, "onAssistStructureFailure ---> ${failure.message}")
    }

    override fun onCreate() {
        super.onCreate()
        AppBus.reg(this)
    }

    override fun onHandleScreenshot(screenshot: Bitmap?) {
        this.screenshot = screenshot
        thread {
            Vog.d(this, "onHandleScreenshot ---> $screenshot")
            if (screenshot != null) {
                UtilBridge.bitmap2File(screenshot, context.cacheDir.absolutePath + "/screen.png")
            }
        }
    }

    override fun onHandleAssist(data: Bundle?, structure: AssistStructure?, content: AssistContent?) {
//        AssistScreenContentDumpThread(context, data, structure, content).start()
        Vog.d(this, "onHandleAssist ---> onHandleAssist")
        if (AppConfig.recoWhenWakeupAssist)
            MainService.switchReco()
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        Vog.d(this, "onKeyLongPress ---> $keyCode")
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onCreateContentView(): View {
        Vog.d(this, "onCreateContentView ---> ")
        val view = layoutInflater.inflate(R.layout.dialog_assist, null)
        return view
    }

    override fun onHide() {
        Vog.d(this, "onHide ---> ")
        AppBus.unreg(this)
        AppBus.post(AppBus.ORDER_CANCEL_RECO)
        finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e: String) {
        when (e) {
            EVENT_HIDE_FLOAT -> hide()
            EVENT_BEGIN_RECO -> {//开始识别

            }
            EVENT_FINISH_RECO -> {

            }
            EVENT_ERROR_RECO -> {

            }
            else -> {
            }
        }
    }

    //    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
//        Vog.d(this,"onKeyUp ---> $keyCode")
//        return super.onKeyUp(keyCode, event)
//    }
//
//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        Vog.d(this,"onKeyDown ---> $keyCode")
//        return super.onKeyDown(keyCode, event)
//    }
}