package cn.vove7.jarvis.speech.baiduspeech.synthesis.util

import android.content.Context
import android.content.res.AssetManager
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.jarvis.services.SpeechSynService.Companion.VOICE_DUXY
import cn.vove7.jarvis.services.SpeechSynService.Companion.VOICE_DUYY
import cn.vove7.jarvis.services.SpeechSynService.Companion.VOICE_FEMALE
import cn.vove7.jarvis.services.SpeechSynService.Companion.VOICE_MALE
import java.io.IOException


/**
 * Baidu语音合成离线资源
 * Created by fujiayi on 2017/5/19.
 */

class OfflineResource(context: Context, voiceType: String) {

    private var assets: AssetManager? = null
    private var destPath: String? = null

    /**
     * 离线文件  bd_etts_text.dat 位置
     * 从assset 复制到sdcard
     */
    var offlineFile: String? = null
        private set

    /**
     * 离线发音人资源
     */
    var modelFilename: String? = null
        private set

    init {
        try {
            val context = context.applicationContext
            this.assets = context.applicationContext.assets
            this.destPath = FileUtil.createTmpDir(context)
            setOfflineVoiceType(voiceType)
        } catch (e: Exception) {
            GlobalLog.err(e)
            GlobalApp.toastError("离线资源加载失败 ${e.message}")
        }
    }

    fun setOfflineVoiceType(voiceType: String) {
        val text = "bd_etts_text.dat"
        val model: String = when (voiceType) {
            VOICE_MALE -> "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat"
            VOICE_FEMALE -> "bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat"
            VOICE_DUXY -> "bd_etts_common_speech_yyjw_mand_eng_high_am-mix_v3.0.0_20170512.dat"
            VOICE_DUYY -> "bd_etts_common_speech_as_mand_eng_high_am_v3.0.0_20170516.dat"
            else -> throw RuntimeException("voice type is not in list")
        }
        try {
            offlineFile = copyAssetsFile(text)
            modelFilename = copyAssetsFile(model)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun copyAssetsFile(sourceFilename: String): String {
        val destFilename = "$destPath/$sourceFilename"
        FileUtil.copyFromAssets(assets, "bd/$sourceFilename", destFilename, false)
        return destFilename
    }

}
