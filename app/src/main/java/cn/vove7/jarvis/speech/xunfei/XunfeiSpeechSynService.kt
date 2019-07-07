//package cn.vove7.jarvis.speech.xunfei
//
//import android.os.Bundle
//import cn.vove7.common.app.AppConfig
//import cn.vove7.common.app.GlobalLog
//import cn.vove7.jarvis.speech.SpeechSynService
//import cn.vove7.jarvis.speech.SyntheEvent
//import com.iflytek.cloud.*
//
///**
// * # XunfeiSpeechSynService
// *
// * @author Vove
// * 2019/6/23
// */
//class XunfeiSpeechSynService(event: SyntheEvent) : SpeechSynService(event) {
//
//    val listener by lazy { XunfeiSynListener(this, event) }
//
//    private lateinit var mTts: SpeechSynthesizer
//
//    override val enableOffline: Boolean = false
//
//    override fun setAudioStream(type: Int) {
//        mTts.setParameter(SpeechConstant.STREAM_TYPE, type.toString())
//    }
//
//    override fun release() {
//        mTts.destroy()
//    }
//
//    override fun doSpeak(text: String) {
//        mTts.startSpeaking(text, listener)
//    }
//
//    override fun doStop() {
//        mTts.stopSpeaking()
//    }
//
//    override fun init() {
//        SpeechUtility.createUtility(context, SpeechConstant.APPID +"=${AppConfig.xunfeiSpeechKey}")
//        mTts = SpeechSynthesizer.createSynthesizer(context) {
//            GlobalLog.log("讯飞初语音合成始化：$it")
//        }
//    }
//
//    override fun doPause() {
//        mTts.pauseSpeaking()
//    }
//
//    override fun doResume() {
//        mTts.resumeSpeaking()
//    }
//}
//
//class XunfeiSynListener(val service: SpeechSynService, val event: SyntheEvent) : SynthesizerListener {
//    override fun onBufferProgress(p0: Int, p1: Int, p2: Int, p3: String?) {
//    }
//
//    override fun onSpeakBegin() {
//    }
//
//    override fun onSpeakProgress(p0: Int, p1: Int, p2: Int) {
//    }
//
//    override fun onEvent(p0: Int, p1: Int, p2: Int, p3: Bundle?) {
//    }
//
//    override fun onSpeakPaused() {
//    }
//
//    override fun onSpeakResumed() {
//    }
//
//    override fun onCompleted(p0: SpeechError?) {
//        p0?.also {
//            GlobalLog.err(it.errorDescription)
//            event.onError(service.speakingText)
//        } ?: event.onFinish(service.speakingText)
//        service.speakingText = null
//    }
//}