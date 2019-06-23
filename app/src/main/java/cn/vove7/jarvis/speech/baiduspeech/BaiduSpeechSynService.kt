package cn.vove7.jarvis.speech.baiduspeech

import cn.vove7.common.app.GlobalLog
import cn.vove7.jarvis.speech.SpeechSynthesizerI
import cn.vove7.jarvis.speech.baiduspeech.synthesis.control.BaiduSynthesizer
import cn.vove7.vtp.log.Vog
import com.baidu.tts.client.SpeechError
import com.baidu.tts.client.SpeechSynthesizerListener
import java.lang.Thread.sleep

/**
 * 语音合成服务
 */
class BaiduSpeechSynService(val event: SyncEvent) : SpeechSynthesizerListener {


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
        Vog.d("reLoad ---> ")
        release()
        sleep(500)
        initialTts() // 初始化TTS引擎
    }


    init {
        initialTts() // 初始化TTS引擎
    }

    fun speak(text: String?) {
        speaking = true//标志放此
        if (text == null) {
            onError(text, SpeechError().apply { description = "文本空" })
            return
        }
        sText = text
        event.onStart(text)//检测后台音乐
        synthesizer.speak(text)
    }

    var sText: String? = null

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

    fun stopIfSpeaking(byUser: Boolean = false) {
        if (speaking) {
            stop(byUser)
        }
    }

    /**
     * 停止合成引擎。即停止播放，合成，清空内部合成队列。
     * @param byUser 是否用户
     */
    fun stop(byUser: Boolean = false) {
        synthesizer.stop()
        val text = sText
        if (byUser)
            event.onUserInterrupt(text)
        if (speaking)
            event.onFinish(text)
        speaking = false
        sText = null
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
//        Vog.d("reLoadVoiceModel 切换离线语音：" + offlineResource.modelFilename)
//
//        val result = synthesizer.loadVoiceModel(offlineResource.modelFilename,
//                offlineResource.offlineFile)
//        checkResult(result, "reLoadVoiceModel")
//    }

    override fun onSynthesizeStart(p0: String?) {
        speaking = true//标志放此
        Vog.v("onSynthesizeStart 准备开始合成,序列号:$p0")
    }

    override fun onSynthesizeDataArrived(p0: String?, p1: ByteArray?, p2: Int) {
        Vog.v("onSpeechProgressChanged $p2 合成进度回调, progress：$p0")
    }

    override fun onSynthesizeFinish(p0: String?) {
        Vog.v("onSynthesizeFinish 合成结束回调, 序列号:$p0")
    }

    override fun onSpeechStart(p0: String?) {
        Vog.v("onSpeechStart 播放开始回调, 序列号:$p0")
    }

    override fun onSpeechProgressChanged(p0: String?, p1: Int) {
        Vog.v("播放进度回调,序列号: $p0 progress：$p1   ")
    }

    override fun onSpeechFinish(p0: String?) {
        Vog.v("onSpeechFinish 播放结束回调 $p0")
        speaking = false
        event.onFinish(sText) //speaking=false
    }

    override fun onError(p0: String?, p1: SpeechError?) {
        val e = "错误发生：${p1?.description} ，错误编码: ${p1?.code} 序列号: $p0 "
        speaking = false
        event.onError(sText)
        GlobalLog.err(e)
        Vog.d(e)
    }

}

interface SyncEvent {
    fun onError(text: String?)
    /**
     * speaking is false
     */
    fun onFinish(text: String?)

    fun onUserInterrupt(text: String?)
    fun onStart(text: String?)
    //检测音乐播放，在合成前
}