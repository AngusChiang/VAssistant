package cn.vove7.jarvis.speech.synthesis.util

import android.content.Context
import android.content.res.AssetManager
import cn.vove7.vtp.log.Vog
import java.io.IOException
import java.util.*


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

    init {
        val context = context.applicationContext
        this.assets = context.applicationContext.assets
        this.destPath = FileUtil.createTmpDir(context)
        setOfflineVoiceType(voiceType)

    }

    fun setOfflineVoiceType(voiceType: String) {
        val text = "bd_etts_text.dat"
        val model: String =  when {
            VOICE_MALE == voiceType -> "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat"
            VOICE_FEMALE == voiceType -> "bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat"
            VOICE_DUXY == voiceType -> "bd_etts_common_speech_yyjw_mand_eng_high_am-mix_v3.0.0_20170512.dat"
            VOICE_DUYY == voiceType -> "bd_etts_common_speech_as_mand_eng_high_am_v3.0.0_20170516.dat"
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
        val existed = mapInitied[sourceFilename] // 启动时完全覆盖一次
        if (existed == null || !existed) {
            recover = true
        }
        FileUtil.copyFromAssets(assets, "bd/$sourceFilename", destFilename, recover)
        Vog.d(this, "copyAssetsFile 文件复制成功：$destFilename")
        return destFilename
    }

    companion object {

        val VOICE_FEMALE = "F"

        val VOICE_MALE = "M"


        val VOICE_DUYY = "Y"

        val VOICE_DUXY = "X"

        private val SAMPLE_DIR = "baiduTTS"

        private val mapInitied = HashMap<String, Boolean>()
    }


}
