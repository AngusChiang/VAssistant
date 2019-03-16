package cn.vove7.jarvis.tools

import android.media.AudioAttributes
import android.media.MediaPlayer
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.utils.ThreadPool
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
        ThreadPool.runOnCachePool {
            val p = MediaPlayer.create(GlobalApp.APP, rawId, AudioAttributes.Builder()
                    .setLegacyStreamType(streamType).build(), 9)
            p?.setOnCompletionListener {
                onFinish?.invoke()
                Vog.d("playOnce ---> 结束")
                it?.release()
            } ?: onFinish?.invoke()
            p?.setOnErrorListener { p, w, e ->
                p.release()
                Vog.d("playOnce 出错 ---> $w, $e")
                onFinish?.invoke()
                true
            }
            p?.start()
        }
    }

}
