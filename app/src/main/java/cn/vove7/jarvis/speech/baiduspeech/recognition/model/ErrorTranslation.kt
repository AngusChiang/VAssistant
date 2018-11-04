package cn.vove7.jarvis.speech.baiduspeech.recognition.model

import android.speech.SpeechRecognizer


object ErrorTranslation {

    fun recogError(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "音频问题"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "没有语音输入"
            SpeechRecognizer.ERROR_CLIENT -> "其它客户端错误"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
            SpeechRecognizer.ERROR_NETWORK -> "网络问题"
            SpeechRecognizer.ERROR_NO_MATCH -> "没有匹配的识别结果"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "引擎忙"
            SpeechRecognizer.ERROR_SERVER -> "服务端错误"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "连接超时"
            else -> "未知错误:$errorCode"
        }
    }

    fun wakeupError(errorCode: Int): String {
        return when (errorCode) {
            1 -> "参数错误"
            2 -> "网络请求发生错误"
            3 -> "服务器数据解析错误"
            4 -> "网络不可用"

            else -> "未知错误:$errorCode"
        }
    }
}
