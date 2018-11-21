package cn.vove7.jarvis.tools

import android.media.AudioAttributes
import android.media.MediaPlayer
import cn.vove7.common.app.GlobalApp
import cn.vove7.executorengine.bridges.SystemBridge
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
     *
     * @param rawId Int
     * @param streamType Int 输出 默认 按 语音合成通道
     * @param onFinish (() -> Unit)?
     */
    fun playOnce(rawId: Int, streamType: Int = AppConfig.currentStreamType,
                 onFinish: (() -> Unit)? = null) {
        SystemBridge.getMusicFocus()
        val p = MediaPlayer.create(GlobalApp.APP, rawId, AudioAttributes.Builder()
                .setLegacyStreamType(streamType).build(), 9)
        p.setOnCompletionListener {
            it?.release()
            onFinish?.invoke()
            Vog.d(this,"playOnce ---> 结束")
            SystemBridge.removeMusicFocus()
        }
        p.start()
    }
}