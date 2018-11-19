package cn.vove7.jarvis.services

import cn.vassistant.plugininterface.app.GlobalLog
import cn.vove7.jarvis.speech.SpeechSynthesizerI
import cn.vove7.jarvis.speech.baiduspeech.synthesis.control.BaiduSynthesizer
import cn.vove7.vtp.log.Vog
import com.baidu.tts.client.SpeechError
import com.baidu.tts.client.SpeechSynthesizerListener
import java.lang.Thread.sleep

/**
 * 语音合成服务
 */
class SpeechSynService(val event: SyncEvent) : SpeechSynthesizerListener {


    companion object {
        const val VOICE_FEMALE = "0"
        const val VOICE_MALE = "1"
        const val VOICE_DUXY = "3"
        const val VOICE_DUYY = "4"
    }
//    var event: SyncEvent? = null

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型

    var speaking = false

    // 主控制类，所有合成控制方法从这个类开始
    lateinit var synthesizer: SpeechSynthesizerI

    fun reLoad() {
        Vog.d(this, "reLoad ---> ")
        release()
        sleep(500)
        initialTts() // 初始化TTS引擎
    }


    init {
        initialTts() // 初始化TTS引擎
    }

    fun speak(text: String?) {
        if (text == null) {
            event.onError("文本空", text)
            return
        }
        sText = text
        event.onStart()//检测后台音乐
        synthesizer.speak(text)
    }

    var sText: String? = null
        get() {
            val v = field
            field = null
            return v
        }

    /**
     * 暂停播放。仅调用speak后生效
     */
    fun pause() {
        synthesizer.pause()
    }

    /**
     * 继续播放。仅调用speak后生效，调用pause生效
     */
    fun resume() {
        synthesizer.resume()
    }

    fun release() {
        synthesizer.release()
    }

    /**
     * 停止合成引擎。即停止播放，合成，清空内部合成队列。
     * @param byUser 是否用户
     */
    fun stop(byUser: Boolean = false) {
        synthesizer.stop()
        speaking = false
        if (byUser)
            event.onUserInterrupt()
        if (speaking)
            event.onFinish()
    }

    private fun initialTts() {
        synthesizer = BaiduSynthesizer(this)
    }

    fun reloadStreamType() {
        synthesizer.reloadStreamType()
    }

    /**
     * 切换离线发音。注意需要添加额外的判断：引擎在合成时该方法不能调用
     */
//    public fun reLoadVoiceModel(mode: String) {
//        voiceModel = mode
//        val offlineResource = OfflineResource(context, voiceModel)
//        Vog.d(this, "reLoadVoiceModel 切换离线语音：" + offlineResource.modelFilename)
//
//        val result = synthesizer.loadVoiceModel(offlineResource.modelFilename,
//                offlineResource.textFilename)
//        checkResult(result, "reLoadVoiceModel")
//    }

    override fun onSynthesizeStart(p0: String?) {
        Vog.d(this, "onSynthesizeStart 准备开始合成,序列号:$p0")
    }

    override fun onSynthesizeDataArrived(p0: String?, p1: ByteArray?, p2: Int) {
        Vog.d(this, "onSpeechProgressChanged $p2 合成进度回调, progress：$p0")
    }

    override fun onSynthesizeFinish(p0: String?) {
        Vog.d(this, "onSynthesizeFinish 合成结束回调, 序列号:$p0")
        speaking = true//
    }

    override fun onSpeechStart(p0: String?) {
        Vog.d(this, "onSpeechStart 播放开始回调, 序列号:$p0")
    }

    override fun onSpeechProgressChanged(p0: String?, p1: Int) {
        Vog.d(this, "播放进度回调,序列号: $p0 progress：$p1   ")
    }

    override fun onSpeechFinish(p0: String?) {
        Vog.d(this, "onSpeechFinish 播放结束回调 $p0")
//        AppBus.post(SpeechSynData(SpeechSynData.SYN_STATUS_FINISH))
        speaking = false
        event.onFinish() //speaking=false
    }

    override fun onError(p0: String?, p1: SpeechError?) {
        val e = "错误发生：${p1?.description} ，错误编码: $p1?.code} 序列号: $p0 "
//        AppBus.post(SpeechSynData(e))
        speaking = false
        event.onError(e, sText)
        GlobalLog.err(e)
        Vog.d(this, e)
    }

}

interface SyncEvent {
    fun onError(err: String, requestText: String?)
    /**
     * speaking is true
     */
    fun onFinish()

    fun onUserInterrupt()
    fun onStart()//检测音乐播放，在合成前！！！//上面监听器中概率误认为有音乐播放
}