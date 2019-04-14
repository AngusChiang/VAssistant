package cn.vove7.jarvis.speech

import java.io.Serializable

/**
 * 语音识别事件interface
 */
interface SpeechEvent {
    companion object {
        const val CODE_UNKNOWN = -1
        const val CODE_NO_RESULT = 1
        const val CODE_NET_ERROR = 2
        const val CODE_NO_RECORDER_PERMISSION = 3
        const val CODE_ENGINE_BUSY = 4
        const val CODE_RECORDER_OPEN_FAIL = 5

        fun codeString(code: Int): String {
            return mapOf(
                    Pair(CODE_UNKNOWN, "未知错误"),
                    Pair(CODE_NO_RESULT, "无识别结果"),
                    Pair(CODE_NET_ERROR, "网络错误"),
                    Pair(CODE_NO_RECORDER_PERMISSION, "无麦克风权限"),
                    Pair(CODE_ENGINE_BUSY, "引擎忙"),
                    Pair(CODE_RECORDER_OPEN_FAIL, "麦克风打开失败")
            )[code] ?: "未知错误"
        }
    }

    /**
     *
     * @param word String?
     * @return Boolean 是否需要继续唤醒识别
     */
    fun onWakeup(word: String?)

    /**
     * 手动开始识别前
     * 未真正开始识别
     */
    fun onPreStartRecog(byVoice: Boolean)

    //准备 播放音效后
    fun onRecogReady(silent: Boolean)

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
    fun onRecogFailed(errCode: Int)

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

    fun onFinish() {}
}

/**
 * 语音识别数据
 */
data class VoiceData(val what: Int = 0, val data: String? = null, val volumePercent: Int = 0)
    : Serializable
