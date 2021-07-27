package cn.vove7.jarvis.speech

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Pair
import androidx.core.app.ActivityCompat
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.runOnNewHandlerThread

/**
 * # SpeechSynService
 *
 * @author Administrator
 * 2018/11/4
 */
abstract class SpeechSynService(val event: SyntheEvent) {

    init {
        runOnNewHandlerThread {
            init()
        }
    }

    val context: Context get() = GlobalApp.APP

    var speaking: Boolean = false

    var speakingText: String? = null

    abstract fun release()

    abstract fun setAudioStream(type: Int)

    abstract fun doSpeak(text: String)

    abstract fun doStop()

    abstract fun init()

    fun pause() {
        event.onPause(speakingText)
        doPause()
    }

    abstract fun doPause()

    abstract fun doResume()

    /**
     * 批量合成 暂不支持
     * @param texts List<Pair<String, String>>
     */
    open fun batchSpeak(texts: List<Pair<String, String>>) {

    }

    fun resume() {
        event.onResume(speakingText)
        doResume()
    }

    fun reloadStreamType() {
        setAudioStream(AppConfig.currentStreamType)
    }


    fun speak(text: String?, showPanel: Boolean = true) {
        if (speaking) {
            stop(false)
        }
        speaking = true//标志放此
        if (text == null) {
            event.onError(text, "文本空")
            return
        }
        speakingText = text
        event.onStart(text, showPanel)//检测后台音乐
        doSpeak(text)
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
        val text = speakingText
        if (byUser)
            event.onUserInterrupt(text)
        if (speaking)
            event.onFinish(text)
        doStop()
        speaking = false
        speakingText = null
    }

    fun reload() {
        release()
        init()
    }


//     fun setStereoVolume(leftVolume: Float, rightVolume: Float)


    fun hasStoragePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}