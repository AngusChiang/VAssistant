package cn.vove7.jarvis.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import cn.vove7.common.app.GlobalApp
import cn.vove7.jarvis.speech.synthesis.control.InitConfig
import cn.vove7.jarvis.speech.synthesis.control.MySyntherizer
import cn.vove7.jarvis.speech.synthesis.control.NonBlockSyntherizer
import cn.vove7.jarvis.speech.synthesis.util.OfflineResource
import cn.vove7.vtp.log.Vog
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy
import com.baidu.tts.client.SpeechError
import com.baidu.tts.client.SpeechSynthesizer
import com.baidu.tts.client.SpeechSynthesizerListener
import com.baidu.tts.client.TtsMode
import java.util.*

/**
 * 语音合成服务
 */
class SpeechSynService(private val event: SyncEvent) : SpeechSynthesizerListener {

    private var appId: String
    private var appKey: String
    private var secretKey: String

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected var ttsMode = TtsMode.MIX

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型
    //TODO sp配置
    protected var offlineVoice = OfflineResource.VOICE_MALE


    // 主控制类，所有合成控制方法从这个类开始
    protected lateinit var synthesizer: MySyntherizer

    private val context: Context = GlobalApp.APP

    init {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName,
                PackageManager.GET_META_DATA)
        appId = appInfo.metaData.get("com.baidu.speech.APP_ID").toString()
        appKey = appInfo.metaData.getString("com.baidu.speech.API_KEY")
        secretKey = appInfo.metaData.getString("com.baidu.speech.SECRET_KEY")

        initialTts() // 初始化TTS引擎
    }

    // 需要合成的文本text的长度不能超过1024个GBK字节。
    // 合成前可以修改参数：
    // Map<String, String> params = getParams();
    // synthesizer.setParams(params);
    public fun speak(text: String?) {
        if(text==null) {
            event.onError("文本空")
            return
        }

        val result = synthesizer.speak(text)
        checkResult(result, "speak")
    }

    /**
     * 暂停播放。仅调用speak后生效
     */
    public fun pause() {
        val result = synthesizer.pause()
        checkResult(result, "pause")
    }

    /**
     * 继续播放。仅调用speak后生效，调用pause生效
     */
    public fun resume() {
        val result = synthesizer.resume()
        checkResult(result, "resume")
    }

    fun release() {
        synthesizer.release()
    }

    /**
     * 停止合成引擎。即停止播放，合成，清空内部合成队列。
     */
    private fun stop() {
        val result = synthesizer.stop()
        checkResult(result, "stop")
    }

    @SuppressLint("HandlerLeak")
    protected fun initialTts() {
        LoggerProxy.printable(true) // 日志打印在logcat中
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类

        val params = getParams()

        val initConfig = InitConfig(appId, appKey, secretKey, ttsMode, params, this)

        synthesizer = NonBlockSyntherizer(context, initConfig)

    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected fun getParams(): Map<String, String> {
        val params = HashMap<String, String>()
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params[SpeechSynthesizer.PARAM_SPEAKER] = "0"
        // 设置合成的音量，0-9 ，默认 5
        params[SpeechSynthesizer.PARAM_VOLUME] = "9"
        // 设置合成的语速，0-9 ，默认 5
        params[SpeechSynthesizer.PARAM_SPEED] = "5"
        // 设置合成的语调，0-9 ，默认 5
        params[SpeechSynthesizer.PARAM_PITCH] = "5"

        params[SpeechSynthesizer.PARAM_MIX_MODE] = SpeechSynthesizer.MIX_MODE_DEFAULT
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        val offlineResource = OfflineResource(context, offlineVoice)
        try {// 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
            params[SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE] = offlineResource.textFilename!!
            params[SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE] = offlineResource.modelFilename!!
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return params
    }

    /**
     * 切换离线发音。注意需要添加额外的判断：引擎在合成时该方法不能调用
     */
    public fun reLoadVoiceModel(mode: String) {
        offlineVoice = mode
        val offlineResource = OfflineResource(context, offlineVoice)
        Vog.d(this, "reLoadVoiceModel 切换离线语音：" + offlineResource.modelFilename)

        val result = synthesizer.loadVoiceModel(offlineResource.modelFilename,
                offlineResource.textFilename)
        checkResult(result, "reLoadVoiceModel")
    }

    private fun checkResult(result: Int, method: String) {
        if (result != 0) {
            Vog.d(this, "checkResult error code :$result method:$method, 错误码文档:http://yuyin.baidu.com/docs/tts/122 ")
        }
    }

    override fun onSynthesizeStart(p0: String?) {
        Vog.d(this, "onSynthesizeStart 准备开始合成,序列号:$p0")
    }

    override fun onSynthesizeDataArrived(p0: String?, p1: ByteArray?, p2: Int) {
        Vog.d(this, "onSpeechProgressChanged $p2 合成进度回调, progress：$p0")
    }

    override fun onSynthesizeFinish(p0: String?) {
        Vog.d(this, "onSynthesizeFinish 合成结束回调, 序列号:$p0")
    }

    override fun onSpeechStart(p0: String?) {
        Vog.d(this, "onSpeechStart 播放开始回调, 序列号:$p0")
        event.onStart()
    }

    override fun onSpeechProgressChanged(p0: String?, p1: Int) {
        Vog.d(this, "播放进度回调,序列号: $p0 progress：$p1   ")
    }

    override fun onSpeechFinish(p0: String?) {
        Vog.d(this, "onSpeechFinish 播放结束回调 $p0")
//        AppBus.post(SpeechSynData(SpeechSynData.SYN_STATUS_FINISH))
        event.onFinish()
    }

    override fun onError(p0: String?, p1: SpeechError?) {
        val e = "错误发生：${p1?.description} ，错误编码: $p1?.code} 序列号: $p0 "
//        AppBus.post(SpeechSynData(e))
        event.onError(e)
        Vog.d(this, e)
    }
}

interface SyncEvent {
    fun onError(err: String)
    fun onFinish()
    fun onStart()
}