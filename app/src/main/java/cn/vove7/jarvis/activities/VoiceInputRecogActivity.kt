package cn.vove7.jarvis.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.RecognizerIntent.ACTION_WEB_SEARCH
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.model.VoiceRecogResult
import cn.vove7.jarvis.services.MainService
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * # VoiceInputRecogActivity
 * 语音识别输入 Activity
 * @author 11324
 * 2019/1/20
 */
class VoiceInputRecogActivity : Activity() {
//    lateinit var vi: VoiceInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppBus.reg(this)
        MainService.startVoiceInput()

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            vi = voiceInteractor
//        }
    }

    //
//    override fun isVoiceInteraction(): Boolean = true
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onResult(voiceResult: VoiceRecogResult) {

//        vi.activeRequests
        val i = Intent()
        val result = voiceResult.result
        if (intent?.action == ACTION_WEB_SEARCH) {
            SystemBridge.quickSearch(result)
        } else {
//            val arr = arrayOf(result,result,result)
            val arr1 = ArrayList<String>()
            arr1.add(result)
//            i.putExtra(RecognizerIntent.EXTRA_RESULTS, arr)
            i.putStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS, arr1)
//            i.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, arrayListOf(1.0))
//            i.putExtra("android.speech.extra.LANGUAGE_RESULTS", "cmn-hans-cn")
            i.putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, floatArrayOf(1f))
            i.putExtra("query", voiceResult.result)
            setResult(RESULT_OK, i)
        }


        finishAndRemoveTask()
    }

    override fun onBackPressed() {
        if (MainService.recogIsListening) {
            MainService.onCommand(AppBus.ACTION_CANCEL_RECOG)
        } else {
            super.onBackPressed()
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isFinishing) {
            finishAndRemoveTask()
        }
        AppBus.unreg(this)
    }

}