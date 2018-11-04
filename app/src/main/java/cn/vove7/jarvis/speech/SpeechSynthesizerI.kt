package cn.vove7.jarvis.speech

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.support.v4.app.ActivityCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.tools.AppConfig
import cn.vove7.vtp.log.Vog

/**
 * # SpeechSynthesizerI
 *
 * @author Administrator
 * 2018/11/4
 */
interface SpeechSynthesizerI {
    val context: Context get() = GlobalApp.APP

    fun release()
    fun setAudioStream(type: Int)
    fun speak(text: String?)
    fun pause()
    fun resume()
    fun stop()
    fun setStereoVolume(leftVolume: Float, rightVolume: Float)
    fun reloadStreamType()

    companion object {
        val streamTypeArray = arrayOf(
                AudioManager.STREAM_MUSIC
                , AudioManager.STREAM_RING
                , AudioManager.STREAM_NOTIFICATION
        )
    }

    val currentStreamType: Int
        get() {
            val i = AppConfig.synStreamIndex.let { if (it in 0..2) it else 0 }
            Vog.d(this, "currentStreamIndex ---> $i")
            return streamTypeArray[i]
        }

    fun hasStoragePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}