package cn.vove7.jarvis.speech.baiduspeech.synthesis.util

import com.baidu.tts.client.SpeechSynthesizerListener
import com.baidu.tts.client.TtsMode

/**
 * 合成引擎的初始化参数
 *
 *
 * Created by fujiayi on 2017/9/13.
 */

class InitConfig(
        /**
         * appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
         * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
         */
        val appId: String, val appKey: String, val secretKey: String,
        /**
         * 纯在线或者离在线融合
         */
        val ttsMode: TtsMode,
        /**
         * 初始化的其它参数，用于setParam
         */
        val params: Map<String, String>,
        /**
         * 合成引擎的回调
         */
        val listener: SpeechSynthesizerListener
)
