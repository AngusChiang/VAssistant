package cn.vove7.jarvis.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.support.v4.app.ActivityCompat
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.jarvis.speech.synthesis.control.InitConfig
import cn.vove7.jarvis.speech.synthesis.control.MySyntherizer
import cn.vove7.jarvis.speech.synthesis.control.NonBlockSyntherizer
import cn.vove7.jarvis.speech.synthesis.util.OfflineResource
import cn.vove7.jarvis.utils.AppConfig
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy
import com.baidu.tts.client.SpeechError
import com.baidu.tts.client.SpeechSynthesizer
import com.baidu.tts.client.SpeechSynthesizerListener
import com.baidu.tts.client.TtsMode
import java.lang.Thread.sleep
import java.util.*

/**
 * 语音合成服务
 */
object SpeechSynService : SpeechSynthesizerListener {

    private var appId: String
    private var appKey: String
    private var secretKey: String

    const val VOICE_FEMALE = "0"
    const val VOICE_MALE = "1"
    const val VOICE_DUXY = "3"
    const val VOICE_DUYY = "4"
    var event: SyncEvent? = null

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    private var ttsMode = TtsMode.MIX

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型

    private var voiceModel = VOICE_FEMALE
    private var voiceSpeed = "5"

    var speaking = false

    // 主控制类，所有合成控制方法从这个类开始
    lateinit var synthesizer: MySyntherizer

    private val context: Context get() = GlobalApp.APP

    fun reLoad() {
        Vog.d(this, "reLoad ---> ")
        release()
        sleep(500)
        initialTts() // 初始化TTS引擎
    }

    private val streamTypeArray = arrayOf(
            AudioManager.STREAM_MUSIC
            , AudioManager.STREAM_RING
            , AudioManager.STREAM_NOTIFICATION
    )

    init {
        val appInfo = context.packageManager.getApplicationInfo(context.packageName,
                PackageManager.GET_META_DATA)
        if (BuildConfig.DEBUG) {
            appId = "11389525"
            appKey = "ILdLUepG75UwwQVa0rqiEUVa"
            secretKey = "di6djKXGGELgnCCusiQUlCBYRxXVrr46"
        } else {
            appId = appInfo.metaData.getInt("com.baidu.speech.APP_ID").toString()
            appKey = appInfo.metaData.getString("com.baidu.speech.API_KEY")!!
            secretKey = appInfo.metaData.getString("com.baidu.speech.SECRET_KEY")!!
        }

        initialTts() // 初始化TTS引擎
    }

    // 需要合成的文本text的长度不能超过1024个GBK字节。
    // 合成前可以修改参数：
    // Map<String, String> params = getParams();
    // synthesizer.setParams(params);
    fun speak(text: String?) {
        if (text == null) {
            event?.onError("文本空", text)
            return
        }
        sText = text
        event?.onStart()//检测后台音乐
        synthesizer.speak(text)
    }

    var sText: String? = null

    /**
     * 暂停播放。仅调用speak后生效
     */
    fun pause() {
        synthesizer.pause()
    }

    /**
     * 继续播放。仅调用speak后生效，调用pause生效
     */
    fun resume() {
        synthesizer.resume()
    }

    fun release() {
        synthesizer.release()
    }

    /**
     * 停止合成引擎。即停止播放，合成，清空内部合成队列。
     */
    fun stop() {
        synthesizer.stop()
    }

    @SuppressLint("HandlerLeak")
    private fun initialTts() {
        val sp = SpHelper(context)
        voiceModel = getTypeCode() ?: VOICE_FEMALE
        var eed = sp.getInt(R.string.key_voice_syn_speed)
        if (eed == -1) eed = 5
        voiceSpeed = eed.toString()

        LoggerProxy.printable(false) // 日志打印在logcat中

        val params = getParams()

        val initConfig = InitConfig(appId, appKey, secretKey, if (hasStoragePermission())
            ttsMode else TtsMode.ONLINE, params, this)
        synthesizer = NonBlockSyntherizer(initConfig)
        reloadStreamType()
    }


    val currentStreamType: Int
        get() {
            val i = AppConfig.synStreamIndex.let { if (it in 0..2) it else 0 }
            Vog.d(this, "currentStreamIndex ---> $i")
            return streamTypeArray[i]
        }

    fun reloadStreamType() {
        synthesizer.setAudioStream(currentStreamType)
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    private fun getParams(): Map<String, String> {
        val params = HashMap<String, String>()
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>

        params[SpeechSynthesizer.PARAM_SPEAKER] = voiceModel
        // 设置合成的音量，0-9 ，默认 5
        params[SpeechSynthesizer.PARAM_VOLUME] = "9"
        // 设置合成的语速，0-9 ，默认 5
        params[SpeechSynthesizer.PARAM_SPEED] = voiceSpeed
        // 设置合成的语调，0-9 ，默认 5
        params[SpeechSynthesizer.PARAM_PITCH] = "5"

        // 不使用压缩传输
//        params[SpeechSynthesizer.PARAM_AUDIO_ENCODE]= SpeechSynthesizer.AUDIO_ENCODE_PCM
//        params[SpeechSynthesizer.PARAM_AUDIO_RATE] = SpeechSynthesizer.AUDIO_BITRATE_PCM

//        params[SpeechSynthesizer.PARAM_MIX_MODE] = SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE
//        params[SpeechSynthesizer.PARAM_MIX_MODE] = SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

//         离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        if (hasStoragePermission()) {
            val offlineResource = OfflineResource(context, voiceModel)
            try {// 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
                params[SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE] = offlineResource.textFilename!!
                params[SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE] = offlineResource.modelFilename!!
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return params
    }

    private fun hasStoragePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun getTypeCode(): String? {
        val sp = SpHelper(context)

        val type = sp.getString(R.string.key_voice_syn_model) ?: return null
        val entity = context.resources.getStringArray(R.array.voice_model_entities)
        val i = entity.indexOf(type)
        val types = context.resources.getStringArray(R.array.voice_model_values)
        return types[i]
    }

    /**
     * 切换离线发音。注意需要添加额外的判断：引擎在合成时该方法不能调用
     */
//    public fun reLoadVoiceModel(mode: String) {
//        voiceModel = mode
//        val offlineResource = OfflineResource(context, voiceModel)
//        Vog.d(this, "reLoadVoiceModel 切换离线语音：" + offlineResource.modelFilename)
//
//        val result = synthesizer.loadVoiceModel(offlineResource.modelFilename,
//                offlineResource.textFilename)
//        checkResult(result, "reLoadVoiceModel")
//    }

    override fun onSynthesizeStart(p0: String?) {
        Vog.d(this, "onSynthesizeStart 准备开始合成,序列号:$p0")
    }

    override fun onSynthesizeDataArrived(p0: String?, p1: ByteArray?, p2: Int) {
        Vog.d(this, "onSpeechProgressChanged $p2 合成进度回调, progress：$p0")
    }

    override fun onSynthesizeFinish(p0: String?) {
        Vog.d(this, "onSynthesizeFinish 合成结束回调, 序列号:$p0")
        speaking = true//
    }

    override fun onSpeechStart(p0: String?) {
        Vog.d(this, "onSpeechStart 播放开始回调, 序列号:$p0")
    }

    override fun onSpeechProgressChanged(p0: String?, p1: Int) {
        Vog.d(this, "播放进度回调,序列号: $p0 progress：$p1   ")
    }

    override fun onSpeechFinish(p0: String?) {
        Vog.d(this, "onSpeechFinish 播放结束回调 $p0")
        speaking = false
//        AppBus.post(SpeechSynData(SpeechSynData.SYN_STATUS_FINISH))
        event?.onFinish()
    }

    override fun onError(p0: String?, p1: SpeechError?) {
        val e = "错误发生：${p1?.description} ，错误编码: $p1?.code} 序列号: $p0 "
//        AppBus.post(SpeechSynData(e))
        speaking = false
        event?.onError(e, sText)
        sText = null
        GlobalLog.err(e)
        Vog.d(this, e)
    }

//    companion object {

//    }

}

interface SyncEvent {
    fun onError(err: String, requestText: String?)
    fun onFinish()
    fun onStart()//检测音乐播放，在合成前！！！//上面监听器中概率误认为有音乐播放
}