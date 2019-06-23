package cn.vove7.jarvis.speech

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import cn.vove7.common.app.GlobalApp

/**
 * # SpeechSynthesizerI
 *
 * @author Administrator
 * 2018/11/4
 */
interface SpeechSynthesizerI {
    val context: Context get() = GlobalApp.APP

    var enableOffline: Boolean

    fun release()
    fun setAudioStream(type: Int)
    fun speak(text: String?)
    fun pause()
    fun resume()
    fun stop()
    fun setStereoVolume(leftVolume: Float, rightVolume: Float)
    fun reloadStreamType()

    fun hasStoragePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}