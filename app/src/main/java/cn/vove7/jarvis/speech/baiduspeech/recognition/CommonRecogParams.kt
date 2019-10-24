package cn.vove7.jarvis.speech.baiduspeech.recognition

import android.content.SharedPreferences
import cn.vove7.jarvis.R
import com.baidu.speech.asr.SpeechConstant
import java.util.*

open class CommonRecogParams {

    /**
     * 字符串格式的参数
     */
    var stringParams = ArrayList<String>()

    /**
     * int格式的参数
     */
    var intParams = ArrayList<String>()

    /**
     * bool格式的参数
     */
    var boolParams = ArrayList<String>()

    init {

        stringParams.addAll(listOf(
                SpeechConstant.VAD,
                SpeechConstant.IN_FILE
        ))
        intParams.addAll(listOf(
                SpeechConstant.VAD_ENDPOINT_TIMEOUT
        ))
        boolParams.addAll(listOf(
                SpeechConstant.ACCEPT_AUDIO_DATA,
                SpeechConstant.ACCEPT_AUDIO_VOLUME
        ))
    }

    open fun fetch(sp: SharedPreferences): Map<String, Any> {
        val map = HashMap<String, Any>()

        parseParamArr(sp, map)

        if (sp.getBoolean("_tips_sound", false)) { // 声音回调
            map[SpeechConstant.SOUND_START] = R.raw.bdspeech_recognition_start
            map[SpeechConstant.SOUND_END] = R.raw.bdspeech_speech_end
            map[SpeechConstant.SOUND_SUCCESS] = R.raw.bdspeech_recognition_success
            map[SpeechConstant.SOUND_ERROR] = R.raw.bdspeech_recognition_error
            map[SpeechConstant.SOUND_CANCEL] = R.raw.recog_cancel
        }

        return map
    }

    /**
     * 根据 stringParams intParams boolParams中定义的参数名称，提取SharedPreferences相关字段
     *
     * @param sp
     * @param map
     */
    private fun parseParamArr(sp: SharedPreferences, map: MutableMap<String, Any>) {
        for (name in stringParams) {
            if (sp.contains(name)) {
                val tmp = sp.getString(name, "")!!.replace(",.*".toRegex(), "").trim { it <= ' ' }
                if ("" != tmp) {
                    map[name] = tmp
                }
            }
        }
        for (name in intParams) {
            if (sp.contains(name)) {
                val tmp = sp.getString(name, "")!!.replace(",.*".toRegex(), "").trim { it <= ' ' }
                if ("" != tmp) {
                    map[name] = Integer.parseInt(tmp)
                }
            }
        }
        for (name in boolParams) {
            if (sp.contains(name)) {
                map[name] = sp.getBoolean(name, false)
            }
        }
    }

    companion object {

        private val TAG = "CommonRecogParams"
    }
}

