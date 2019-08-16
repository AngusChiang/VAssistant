package cn.vove7.jarvis.speech.baiduspeech

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.RECORDSTATE_RECORDING
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import cn.vove7.vtp.log.Vog
import java.io.IOException
import java.io.InputStream

/**
 * Baidu语音识别输入
 * @property mAudioRecord AudioRecord
 */
@Suppress("unused")
class MicInputStream : InputStream() {
    private lateinit var mAudioRecord: AudioRecord


    init {
        Vog.d("打开麦克风")
        try {
            this.mAudioRecord = AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                    16000, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, DEFAULT_BUFFER_SIZE)
            initAEC(mAudioRecord.audioSessionId)

            Vog.d("startRecordingAndCheckStatus recorder status is " + this.mAudioRecord.state)
            this.mAudioRecord.startRecording()
            var var3 = 0
            val var4 = ByteArray(32)

            for (var5 in 0..9) {
                val var6 = this.mAudioRecord.read(var4, 0, var4.size)
                if (var6 > 0) {
                    var3 += var6
                    break
                }
            }

            if (var3 <= 0) {
                this.mAudioRecord.release()
                throw Exception("bad recorder, read(byte[])")
            }
        } catch (var15: Exception) {
            var15.printStackTrace()
        } finally {
            run {
                if (this.mAudioRecord.recordingState == RECORDSTATE_RECORDING) {
                    val var10000 = this.mAudioRecord.state
                    if (var10000 != 0) {
                        return@run
                    }
                }
                try {
                    this.mAudioRecord.release()
                } catch (var14: Exception) {
                    var14.printStackTrace()
                }
                Vog.d("recorder start failed, RecordingState=" + this.mAudioRecord.recordingState)
            }

        }

    }

    //消除回音
    private fun initAEC(audioSession: Int): Boolean {

        if (!AcousticEchoCanceler.isAvailable()) {
            Vog.d("AcousticEchoCanceler 不可用")
            return false
        }
        val canceler = AcousticEchoCanceler.create(audioSession) ?: return false
        canceler.enabled = true
        val r = canceler.enabled
        Vog.d("AcousticEchoCanceler enable：$r")
        return r
    }

    @Throws(IOException::class)
    override fun read(var1: ByteArray, var2: Int, var3: Int): Int {
        val var4 = mAudioRecord.read(var1, var2, var3)
        Vog.d("AudioRecord read: len:$var4 byteOffset:$var2 byteCount:$var3")
        return if (var4 in 0..var3) {
            var4
        } else {
            throw IOException("audio recorder read error, len = $var4")
        }
    }

    @Throws(IOException::class)
    override fun close() {
        if (this.mAudioRecord != null) {
            this.mAudioRecord!!.release()
        }

    }

    @Throws(IOException::class)
    override fun read(): Int {
        throw IOException("read not support")
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 160000

        @JvmStatic
        fun instance(): MicInputStream {
            return MicInputStream()
        }
    }
}