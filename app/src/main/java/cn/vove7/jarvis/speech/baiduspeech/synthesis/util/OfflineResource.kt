package cn.vove7.jarvis.speech.baiduspeech.synthesis.util

import android.content.Context
import android.content.res.AssetManager
import cn.vove7.jarvis.services.SpeechSynService.VOICE_DUXY
import cn.vove7.jarvis.services.SpeechSynService.VOICE_DUYY
import cn.vove7.jarvis.services.SpeechSynService.VOICE_FEMALE
import cn.vove7.jarvis.services.SpeechSynService.VOICE_MALE
import java.io.IOException


/**
 * Created by fujiayi on 2017/5/19.
 */

class OfflineResource(context: Context, voiceType: String) {

    private val assets: AssetManager
    private val destPath: String

    var textFilename: String? = null
        private set
    var modelFilename: String? = null
        private set

    private val mapInitied = HashMap<String, Boolean>()
    init {
        val context = context.applicationContext
        this.assets = context.applicationContext.assets
        this.destPath = FileUtil.createTmpDir(context)
        setOfflineVoiceType(voiceType)
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
            textFilename = copyAssetsFile(text)
            modelFilename = copyAssetsFile(model)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun copyAssetsFile(sourceFilename: String): String {
        val destFilename = "$destPath/$sourceFilename"
        var recover = false
        val existed = mapInitied[sourceFilename]?:false // 启动时完全覆盖一次
        if (!existed) {
            recover = true
        }
        FileUtil.copyFromAssets(assets, "bd/$sourceFilename", destFilename, recover)
//        Vog.d(this, "copyAssetsFile 文件复制成功：$destFilename")
        return destFilename
    }

}
