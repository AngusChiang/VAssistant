package cn.vove7.jarvis.speech.baiduspeech.synthesis.control

import android.media.AudioManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Pair
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.app.log
import cn.vove7.common.utils.StorageHelper
import cn.vove7.jarvis.R
import cn.vove7.jarvis.services.SpeechSynService
import cn.vove7.jarvis.speech.SpeechSynthesizerI
import cn.vove7.jarvis.speech.baiduspeech.synthesis.util.OfflineResource
import cn.vove7.common.app.AppConfig
import cn.vove7.jarvis.tools.BaiduKey
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy
import com.baidu.tts.client.SpeechSynthesizeBag
import com.baidu.tts.client.SpeechSynthesizer
import com.baidu.tts.client.SpeechSynthesizerListener
import com.baidu.tts.client.TtsMode
import java.io.File
import java.util.*

/**
 * 百度SpeechSynthesizer
 *
 */
class BaiduSynthesizer(lis: SpeechSynthesizerListener) : SpeechSynthesizerI {
    private lateinit var mSpeechSynthesizer: SpeechSynthesizer
    override val enableOffline: Boolean
        get() = File("${StorageHelper.sdPath}/baiduTTS", "bd_etts_text.dat")
                .exists()

    private var appId: String = BaiduKey.appId.toString()
    private var appKey: String = BaiduKey.appKey
    private var secretKey: String = BaiduKey.sKey

    private lateinit var hThread: HandlerThread
    private lateinit var tHandler: Handler


    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    private val ttsMode
        get() = if (enableOffline) TtsMode.MIX else TtsMode.ONLINE

    private var voiceModel = SpeechSynService.VOICE_FEMALE
    private var voiceSpeed = "5"

    init {

        val sp = SpHelper(context)
        voiceModel = getTypeCode() ?: SpeechSynService.VOICE_FEMALE
        var eed = sp.getInt(R.string.key_voice_syn_speed)
        if (eed == -1) eed = 5
        voiceSpeed = eed.toString()

        LoggerProxy.printable(false) // 日志打印在logcat中

        val params = buildParams()
        val config = InitConfig(appId, appKey, secretKey, if (hasStoragePermission())
            ttsMode else TtsMode.ONLINE, params, lis)
        initThread()
        runInHandlerThread(INIT, config)
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
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    private fun buildParams(): Map<String, String> {
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

//         离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        if (enableOffline) {
            GlobalLog.log("加载百度语音合成离线资源...")
            val offlineResource = OfflineResource(voiceModel)
            try {// 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
                params[SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE] = offlineResource.offlineFile
                params[SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE] = offlineResource.modelFilename
            } catch (e: Exception) {
                GlobalLog.log("语音合成离线资源加载失败：${e.message}")
                e.log()
            }
        }
        Vog.d("合成参数：$params")
        return params
    }


    private fun initThread() {
        hThread = HandlerThread("NonBlockSyntherizer-thread")
        hThread.start()
        tHandler = object : Handler(hThread.looper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    INIT -> {
                        val config = msg.obj as InitConfig
                        val isSuccess = load(config)
                        if (!isSuccess) {
                            GlobalApp.toastError("语音合成引擎初始化失败")
                        }
                    }
                    RELEASE -> releaseA()
                    else -> {
                    }
                }
            }
        }
    }

    override fun release() {
        runInHandlerThread(RELEASE)
        hThread.quitSafely()
    }

    private fun runInHandlerThread(action: Int, obj: Any? = null) {
        val msg = Message.obtain()
        msg.what = action
        msg.obj = obj
        tHandler.sendMessage(msg)
    }

    /**
     * 注意该方法需要在新线程中调用。且该线程不能结束。详细请参见NonBlockSyntherizer的实现
     *
     * @param config InitConfig
     * @return
     */
    protected fun load(config: InitConfig): Boolean {
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

    override fun reloadStreamType() {
        val currentStreamType = AppConfig.currentStreamType
        Vog.d("reloadStreamType ---> $currentStreamType")
        setAudioStream(currentStreamType)
    }

    /**
     * [AudioManager]
     * @param type Int
     */
    override fun setAudioStream(type: Int) {
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
    override fun speak(text: String?) {
        mSpeechSynthesizer.speak(text ?: "?")
    }

    /**
     * 合成并播放
     *
     * @param text        小于1024 GBK字节，即512个汉字或者字母数字
     * @param utteranceId 用于listener的回调，默认"0"
     * @return
     */
    fun speak(text: String, utteranceId: String) {
        checkResult(mSpeechSynthesizer.speak(text, utteranceId), "speak")
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
    fun batchSpeak(texts: List<Pair<String, String>>): Int {
        val bags = ArrayList<SpeechSynthesizeBag>()
        for (pair in texts) {
            val speechSynthesizeBag = SpeechSynthesizeBag()
            speechSynthesizeBag.text = pair.first
            if (pair.second != null) {
                speechSynthesizeBag.utteranceId = pair.second
            }
            bags.add(speechSynthesizeBag)
        }
        return mSpeechSynthesizer.batchSpeak(bags)
    }

    private fun setParams(params: Map<String, String>) {
        for ((key, value) in params) {
            mSpeechSynthesizer.setParam(key, value)
        }
    }

    override fun pause() {
        checkResult(mSpeechSynthesizer.pause(), "speak")
    }

    override fun resume() {
        checkResult(mSpeechSynthesizer.resume(), "resume")
    }

    override fun stop() {
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
    override fun setStereoVolume(leftVolume: Float, rightVolume: Float) {
        mSpeechSynthesizer.setStereoVolume(leftVolume, rightVolume)
    }

    private fun releaseA() {
        mSpeechSynthesizer.stop()
        mSpeechSynthesizer.release()
        isInited = false
    }


    companion object {
        private var isInited = false
        private val INIT = 1
        private val RELEASE = 11
    }
}
