package cn.vove7.jarvis.speech.baiduspeech

import android.media.AudioManager
import android.util.Pair
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.utils.StorageHelper
import cn.vove7.common.utils.runOnNewHandlerThread
import cn.vove7.jarvis.R
import cn.vove7.jarvis.speech.SpeechSynService
import cn.vove7.jarvis.speech.SyntheEvent
import cn.vove7.jarvis.speech.baiduspeech.synthesis.util.InitConfig
import cn.vove7.jarvis.speech.baiduspeech.synthesis.util.OfflineResource
import cn.vove7.jarvis.tools.BaiduKey
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy
import com.baidu.tts.client.*
import java.io.File
import java.util.*

/**
 * 语音合成服务
 */
class BaiduSpeechSynService(event: SyntheEvent) : SpeechSynService(event) {

    private lateinit var mSpeechSynthesizer: SpeechSynthesizer

    var enableOffline: Boolean =
        File("${StorageHelper.extPath}/baiduTTS", "bd_etts_text.dat")
                .exists()


    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    private val ttsMode
        get() = if (enableOffline) TtsMode.MIX else TtsMode.ONLINE

    private var voiceModel: String? = null
        set(value) {
            Vog.d("切换发音人：$value")
            field = value
        }
    private var voiceSpeed = "5"

    override fun init() {

        val appId: String = BaiduKey.appId
        val appKey: String = BaiduKey.appKey
        val secretKey: String = BaiduKey.sKey

        val sp = SpHelper(context)
        voiceModel = getTypeCode()
        var eed = sp.getInt(R.string.key_voice_syn_speed)
        if (eed == -1) eed = 5
        voiceSpeed = eed.toString()

        LoggerProxy.printable(false) // 日志打印在logcat中

        val params = buildParams()
        val config = InitConfig(appId, appKey, secretKey, if (hasStoragePermission())
            ttsMode else TtsMode.ONLINE, params, BaiduSyncLis(this, event))
        runOnNewHandlerThread {
            val isSuccess = load(config)
            if (!isSuccess) {
                GlobalApp.toastError("语音合成引擎初始化失败")
            }
        }
    }

    private fun getTypeCode(): String? {
        val sp = SpHelper(context)

        val i = try {
            context.resources.getStringArray(R.array.voice_model_entities).indexOf(sp.getString(R.string.key_voice_syn_model)
                ?: return null)
        } catch (e: ClassCastException) {
            sp.getInt(R.string.key_voice_syn_model)
        }
        val types = context.resources.getStringArray(R.array.voice_model_values)
        return (types[i] ?: "0").also {
            Vog.d("发音人：$i $it")
        }
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    private fun buildParams(): Map<String, String> {
        val params = HashMap<String, String>()
        // 以下参数均为选填

        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params[SpeechSynthesizer.PARAM_SPEAKER] = voiceModel ?: VOICE_FEMALE
        // 设置合成的音量，0-9 ，默认 5
        params[SpeechSynthesizer.PARAM_VOLUME] = "9"
        // 设置合成的语速，0-9 ，默认 5
        params[SpeechSynthesizer.PARAM_SPEED] = voiceSpeed
        // 设置合成的语调，0-9 ，默认 5
        params[SpeechSynthesizer.PARAM_PITCH] = "5"

//         离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        if (enableOffline) {
            GlobalLog.log("加载百度语音合成离线资源...")
            val offlineResource = OfflineResource(voiceModel ?: VOICE_FEMALE)
            try {// 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
                params[SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE] = offlineResource.offlineFile
                params[SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE] = offlineResource.modelFilename
            } catch (e: Exception) {
                GlobalLog.log("语音合成离线资源加载失败：${e.message}")
                enableOffline = false
            }
        }
        Vog.d("合成参数：$params")
        return params
    }

    override fun release() {
        mSpeechSynthesizer.stop()
        mSpeechSynthesizer.release()
        isInited = false
    }


    /**
     * 注意该方法需要在新线程中调用。且该线程不能结束。详细请参见NonBlockSyntherizer的实现
     *
     * @param config InitConfig
     * @return
     */
    private fun load(config: InitConfig): Boolean {
        if (isInited) return true
        Vog.d("init ---> 初始化开始")
        val isMix = config.ttsMode == TtsMode.MIX
        mSpeechSynthesizer = SpeechSynthesizer.getInstance()
        mSpeechSynthesizer.setContext(context)
        mSpeechSynthesizer.setSpeechSynthesizerListener(config.listener)

        // 请替换为语音开发者平台上注册应用得到的App ID ,AppKey ，Secret Key ，填写在SynthActivity的开始位置
        mSpeechSynthesizer.setAppId(config.appId)
        mSpeechSynthesizer.setApiKey(config.appKey, config.secretKey)

        if (isMix) {
            // 授权检测接口(只是通过AuthInfo进行检验授权是否成功。选择纯在线可以不必调用auth方法。
            val authInfo = mSpeechSynthesizer.auth(config.ttsMode)
            if (!authInfo.isSuccess) {
                // 离线授权需要网站上的应用填写包名。本demo的包名是com.baidu.tts.sample，定义在build.gradle中
                val errorMsg = authInfo.ttsError.detailMessage
                GlobalLog.err("鉴权失败 =$errorMsg")
                return false
            } else {
                Vog.d("init ---> 验证通过，离线正式授权文件存在。")
            }
        }
        setParams(config.params)
        reloadStreamType()
        // 初始化tts
        val result = mSpeechSynthesizer.initTts(config.ttsMode)
        if (result != 0) {
            GlobalLog.err("[error] initTts 初始化失败 + errorCode：$result")
            return false
        }
        // 此时可以调用 speak和synthesize方法
        Vog.d("load ---> 合成引擎初始化成功")
        return true
    }

    /**
     * [AudioManager]
     * @param type Int
     */
    override fun setAudioStream(type: Int) {
        Vog.d("setAudioStream $type")
        mSpeechSynthesizer.setAudioStreamType(type)
    }

    /**
     * 合成并播放
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     * 合成前可以修改参数：
     * Map<String, String> params = buildParams();
     * synthesizer.setParams(params);
     * @param text 小于1024 GBK字节，即512个汉字或者字母数字
     * @return
     */
    override fun doSpeak(text: String) {
        Vog.d("发音人：$voiceModel")
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, voiceModel)
        mSpeechSynthesizer.speak(text)
    }

    private fun checkResult(result: Int?, method: String) {
        if (result != 0) {
            GlobalLog.err("checkResult error code :$result method:$method")// +
//                    "错误码文档:http://yuyin.baidu.com/docs/tts/122 ")
        }
    }

    /**
     * 批量合成
     *
     * @param texts 文本s first 文本 second 序列号
     * @return ?
     */
    override fun batchSpeak(texts: List<Pair<String, String>>) {
        val bags = ArrayList<SpeechSynthesizeBag>()
        for (pair in texts) {
            val speechSynthesizeBag = SpeechSynthesizeBag()
            speechSynthesizeBag.text = pair.first
            if (pair.second != null) {
                speechSynthesizeBag.utteranceId = pair.second
            }
            bags.add(speechSynthesizeBag)
        }
        mSpeechSynthesizer.batchSpeak(bags)
    }

    private fun setParams(params: Map<String, String>) {
        for ((key, value) in params) {
            mSpeechSynthesizer.setParam(key, value)
        }
    }

    override fun doPause() {
        checkResult(mSpeechSynthesizer.pause(), "speak")
    }

    override fun doResume() {
        checkResult(mSpeechSynthesizer.resume(), "resume")
    }

    override fun doStop() {
        checkResult(mSpeechSynthesizer.stop(), "stop")
    }

    /**
     * 引擎在合成时该方法不能调用！！！
     * 注意 只有 TtsMode.MIX 才可以切换离线发音
     *
     * @return res
     */
    fun loadVoiceModel(modelFilename: String?, textFilename: String?): Int {
        val res = mSpeechSynthesizer.loadModel(modelFilename, textFilename)
        Vog.d("load ---> 切换离线发音人成功")
        return res
    }

    /**
     * 设置播放音量，默认已经是最大声音
     * 0.0f为最小音量，1.0f为最大音量
     *
     * @param leftVolume  [0-1] 默认1.0f
     * @param rightVolume [0-1] 默认1.0f
     */
//    override fun setStereoVolume(leftVolume: Float, rightVolume: Float) {
//        mSpeechSynthesizer.setStereoVolume(leftVolume, rightVolume)
//    }


    companion object {
        private var isInited = false

        const val VOICE_FEMALE = "0"
        const val VOICE_MALE = "1"
        const val VOICE_DUXY = "3"
        const val VOICE_DUYY = "4"
        const val VOICE_BOWEN = "106"
        const val VOICE_XIAOTONG = "110"
        const val VOICE_XIAOMENG = "111"
        const val VOICE_MIDUO = "103"
        const val VOICE_XIAOJIAO = "5"
    }


}

class BaiduSyncLis(val service: SpeechSynService, val event: SyntheEvent) : SpeechSynthesizerListener {

    override fun onSynthesizeStart(p0: String?) {
        service.speaking = true//标志放此
        Vog.v("onSynthesizeStart 准备开始合成,序列号:$p0")
    }

    override fun onSynthesizeDataArrived(p0: String?, p1: ByteArray?, p2: Int, p3: Int) {
        Vog.v("onSpeechProgressChanged $p2 合成进度回调, progress：$p0")
    }

    override fun onSynthesizeFinish(p0: String?) {
        Vog.v("onSynthesizeFinish 合成结束回调, 序列号:$p0")
    }

    override fun onSpeechStart(p0: String?) {
        Vog.v("onSpeechStart 播放开始回调, 序列号:$p0")
    }

    override fun onSpeechProgressChanged(p0: String?, p1: Int) {
        Vog.v("播放进度回调,序列号: $p0 progress：$p1   ")
    }

    override fun onSpeechFinish(p0: String?) {
        Vog.v("onSpeechFinish 播放结束回调 $p0")
        service.speaking = false
        event.onFinish(p0 ?: "") //speaking=false
    }

    override fun onError(p0: String?, p1: SpeechError?) {
        val e = "错误发生：${p1?.description} ，错误编码: ${p1?.code} 序列号: $p0 "
        service.speaking = false
        event.onError(p0 ?: "", p1?.description)
        GlobalLog.err(e)
        Vog.d(e)
    }

}
