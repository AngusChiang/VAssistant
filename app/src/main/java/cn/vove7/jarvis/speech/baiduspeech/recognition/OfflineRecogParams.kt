package cn.vove7.jarvis.speech.baiduspeech.recognition

import android.Manifest
import android.app.Activity
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.helper.AdvanAppHelper
import cn.vove7.common.helper.AdvanContactHelper
import cn.vove7.jarvis.services.BaiduSpeechRecogService
import com.baidu.speech.asr.SpeechConstant
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by fujiayi on 2017/6/13.
 */

class OfflineRecogParams(context: Activity) : CommonRecogParams(context) {

    companion object {
        private val offlineWordParams: Map<String, Any>
            get() = mapOf(
                    Pair(SpeechConstant.DECODER, 2),
                    Pair(SpeechConstant.ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH, "asset:///bd/baidu_speech_grammar.bsg"),
                    Pair(SpeechConstant.SLOT_DATA, BaiduSpeechRecogService.OffWord(
                            if (ActivityCompat.checkSelfPermission(GlobalApp.APP,
                                            Manifest.permission.READ_CONTACTS)
                                    != PackageManager.PERMISSION_GRANTED) //首次启动无权限 不做
                                arrayOf() else AdvanContactHelper.getContactName()
                            , AdvanAppHelper.getAppName()
                    ).toString())
            )

        fun fetchOfflineParams(): Map<String, Any> {
            return offlineWordParams
        }

    }

}
