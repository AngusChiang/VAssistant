package cn.vove7.jarvis.speech.model

interface IStatus {
    companion object {

        const val STATUS_NONE = 2

        const val STATUS_READY = 3
        const val STATUS_SPEAKING = 4
        const val STATUS_RECOGNITION = 5

        const val STATUS_FINISHED = 6
        const val STATUS_STOPPED = 10

        const val STATUS_WAITING_READY = 8001
        const val WHAT_MESSAGE_STATUS = 9001

        const val STATUS_WAKEUP_SUCCESS = 7001
        const val STATUS_WAKEUP_EXIT = 7003
    }
}
