package cn.vove7.jarvis.speech.recognition

import android.app.Activity
import android.content.SharedPreferences
import com.baidu.speech.asr.SpeechConstant
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by fujiayi on 2017/6/13.
 */

class OfflineRecogParams(context: Activity) : CommonRecogParams(context) {


    override fun fetch(sp: SharedPreferences): Map<String, Any> {

        val map = super.fetch(sp) as HashMap
        map[SpeechConstant.DECODER] = 2
        return map

    }

    companion object {
        fun fetchOfflineParams(): Map<String, Any> {
            val map = HashMap<String, Any>()
            map[SpeechConstant.DECODER] = 2
            map[SpeechConstant.ASR_OFFLINE_ENGINE_GRAMMER_FILE_PATH] = "asset:///baidu_speech_grammar.bsg"
            map.putAll(fetchSlotDataParam())
            return map
        }

        fun fetchSlotDataParam(): Map<String, Any> {
            val map = HashMap<String, Any>()
            try {
                val json = JSONObject()
                json.put("name", JSONArray().put("赵六").put("赵六"))
                        .put("appname", JSONArray().put("手百").put("度秘"))
                map[SpeechConstant.SLOT_DATA] = json
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return map
        }
    }

}
