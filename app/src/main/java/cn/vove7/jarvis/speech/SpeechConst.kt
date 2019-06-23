package cn.vove7.jarvis.speech

interface SpeechConst {
    companion object {

        const val STATUS_NONE = 2

        const val STATUS_READY = 3
        const val STATUS_SPEAKING = 4
        const val STATUS_RECOGNITION = 5

        const val CODE_WAKEUP_SUCCESS = 7001
        const val CODE_WAKEUP_EXIT = 7003


        const val CODE_VOICE_READY = 110 //准备识别
        const val CODE_VOICE_TEMP = 111 //临时结果
        const val CODE_VOICE_VOL = 112 //音量数据
        const val CODE_VOICE_ERR = 114 //出错
        const val CODE_VOICE_RESULT = 113 //识别结果


        const val STATUS_FINISHED = 6
        const val STATUS_EXIT = 7
        const val STATUS_STOPPED = 10

        const val STATUS_WAITING_READY = 8001
        const val WHAT_MESSAGE_STATUS = 9001

    }
}
