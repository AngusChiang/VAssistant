package cn.vove7.jarvis.speech.baiduspeech.synthesis.util

import cn.vove7.common.utils.StorageHelper
import cn.vove7.jarvis.services.SpeechSynService.Companion.VOICE_DUXY
import cn.vove7.jarvis.services.SpeechSynService.Companion.VOICE_DUYY
import cn.vove7.jarvis.services.SpeechSynService.Companion.VOICE_FEMALE
import cn.vove7.jarvis.services.SpeechSynService.Companion.VOICE_MALE
import java.io.File


/**
 * Baidu语音合成离线资源
 * Created by fujiayi on 2017/5/19.
 */

class OfflineResource(private val voiceType: String) {

    private var destPath: String = StorageHelper.sdPath + "/baiduTTS"

    /**
     * 离线文件 bd_etts_text.dat 位置
     */
    val offlineFile: String get() = "$destPath/bd_etts_text.dat"

    /**
     * 离线发音人资源
     */
    val modelFilename: String
        get() {
            val modelFile: String = "$destPath/" + when (voiceType) {
                VOICE_MALE -> "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat"
                VOICE_FEMALE -> "bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat"
                VOICE_DUXY -> "bd_etts_common_speech_yyjw_mand_eng_high_am-mix_v3.0.0_20170512.dat"
                VOICE_DUYY -> "bd_etts_common_speech_as_mand_eng_high_am_v3.0.0_20170516.dat"
                else -> throw RuntimeException("离线合成发音人类型不支持：voiceType")
            }
            if (File(modelFile).exists()) {
                return modelFile
            }
            throw Exception("离线发音人文件不存在：$modelFile")
        }

}
