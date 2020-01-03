package cn.vove7.jarvis.tools

import android.media.AudioAttributes
import android.media.MediaPlayer
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.CoroutineExt
import cn.vove7.vtp.log.Vog

/**
 * # AudioController
 *
 * @author Administrator
 * 2018/11/21
 */
object AudioController {
    /**
     * 播放raw文件
     * 异步执行 一般才会触发OnCompletion
     * @param rawId Int
     * @param streamType Int 输出 默认 按 语音合成通道
     * @param onFinish (() -> Unit)?
     */
    fun playOnce(rawId: Int, streamType: Int = AppConfig.currentStreamType,
                 onFinish: (() -> Unit)? = null) {
        CoroutineExt.launch {
            MediaPlayer.create(GlobalApp.APP, rawId)?.apply {
                setOnCompletionListener {
                    release()
                    onFinish?.invoke()
                    Vog.d("playOnce ---> 结束")
                }
                setOnErrorListener { _, w, e ->
                    release()
                    Vog.d("playOnce 出错 ---> $w, $e")
                    onFinish?.invoke()
                    true
                }
                this.setAudioAttributes(AudioAttributes.Builder().setLegacyStreamType(streamType).build())

                start()
            } ?: onFinish?.invoke()
        }
    }

}
