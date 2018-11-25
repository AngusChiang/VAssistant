package cn.vove7.jarvis.speech

import cn.vove7.common.appbus.VoiceData

/**
 * 语音识别事件interface
 */
interface SpeechEvent {
    /**
     *
     * @param word String?
     * @return Boolean 是否需要继续唤醒识别
     */
    fun onWakeup(word: String?)

    /**
     * 开始识别
     * 反馈效果
     * 未真正开始识别
     */
    fun onStartRecog(byVoice: Boolean)

    /**
     * 识别成功结果
     * @param voiceResult String
     */
    fun onResult(voiceResult: String)

    /**
     * 中间结果
     * @param temp String
     */
    fun onTempResult(temp: String)

    /**
     * 识别出错
     * @param err String
     */
    fun onRecogFailed(err: String)

    /**
     * 音量事件
     * @param data VoiceData
     */
    fun onVolume(data: VoiceData)

    /**
     * 手动停止聆听
     */
    fun onStopRecog()

    /**
     * 手动取消
     */
    fun onCancelRecog()
}
