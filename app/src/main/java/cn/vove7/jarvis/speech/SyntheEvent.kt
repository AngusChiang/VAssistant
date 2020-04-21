package cn.vove7.jarvis.speech

/**
 * 语音合成事件
 */
interface SyntheEvent {
    fun onError(text: String?)

    fun onPause(text: String?) {}
    fun onResume(text: String?) {}
    /**
     * speaking is false
     */
    fun onFinish(text: String?)

    fun onUserInterrupt(text: String?)
    fun onStart(text: String?, showPanel: Boolean)
    //检测音乐播放，在合成前
}