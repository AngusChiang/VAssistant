package cn.vove7.jarvis.speech.synthesis.control

import android.content.Context
import android.util.Log
import android.util.Pair
import cn.vove7.vtp.log.Vog

import com.baidu.tts.client.SpeechSynthesizeBag
import com.baidu.tts.client.SpeechSynthesizer
import com.baidu.tts.client.TtsMode

import java.util.ArrayList

/**
 * SpeechSynthesizer封装
 *
 *
 */

open class MySyntherizer protected constructor(protected var context: Context) {

    protected var mSpeechSynthesizer: SpeechSynthesizer? = null

    private val isCheckFile = true

    constructor(context: Context, initConfig: InitConfig) : this(context) {
        init(initConfig)
    }

    init {
        if (isInited) {
            // SpeechSynthesizer.getInstance() 不要连续调用
            throw RuntimeException("MySynthesizer 类里面 SpeechSynthesizer还未释放，请勿新建一个新类")
        }
        isInited = true
    }

    /**
     * 注意该方法需要在新线程中调用。且该线程不能结束。详细请参见NonBlockSyntherizer的实现
     *
     * @param config InitConfig
     * @return
     */
    protected fun init(config: InitConfig): Boolean {

        sendToUiThread("初始化开始")
        val isMix = config.ttsMode == TtsMode.MIX
        mSpeechSynthesizer = SpeechSynthesizer.getInstance()
        mSpeechSynthesizer!!.setContext(context)
        mSpeechSynthesizer!!.setSpeechSynthesizerListener(config.listener)


        // 请替换为语音开发者平台上注册应用得到的App ID ,AppKey ，Secret Key ，填写在SynthActivity的开始位置
        mSpeechSynthesizer!!.setAppId(config.appId)
        mSpeechSynthesizer!!.setApiKey(config.appKey, config.secretKey)

        if (isMix) {
            // 授权检测接口(只是通过AuthInfo进行检验授权是否成功。选择纯在线可以不必调用auth方法。
            val authInfo = mSpeechSynthesizer!!.auth(config.ttsMode)
            if (!authInfo.isSuccess) {
                // 离线授权需要网站上的应用填写包名。本demo的包名是com.baidu.tts.sample，定义在build.gradle中
                val errorMsg = authInfo.ttsError.detailMessage
                sendToUiThread("鉴权失败 =$errorMsg")
                return false
            } else {
                sendToUiThread("验证通过，离线正式授权文件存在。")
            }
        }
        setParams(config.params)
        // 初始化tts
        val result = mSpeechSynthesizer!!.initTts(config.ttsMode)
        if (result != 0) {
            sendToUiThread("【error】initTts 初始化失败 + errorCode：$result")
            return false
        }
        // 此时可以调用 speak和synthesize方法
        sendToUiThread("合成引擎初始化成功")
        return true
    }

    /**
     * 合成并播放
     *
     * @param text 小于1024 GBK字节，即512个汉字或者字母数字
     * @return
     */
    fun speak(text: String): Int {
        return mSpeechSynthesizer!!.speak(text)
    }

    /**
     * 合成并播放
     *
     * @param text        小于1024 GBK字节，即512个汉字或者字母数字
     * @param utteranceId 用于listener的回调，默认"0"
     * @return
     */
    fun speak(text: String, utteranceId: String): Int {
        return mSpeechSynthesizer!!.speak(text, utteranceId)
    }

    /**
     * 只合成不播放
     *
     * @param text
     * @return
     */
    fun synthesize(text: String): Int {
        return mSpeechSynthesizer!!.synthesize(text)
    }

    fun synthesize(text: String, utteranceId: String): Int {
        return mSpeechSynthesizer!!.synthesize(text, utteranceId)
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
        return mSpeechSynthesizer!!.batchSpeak(bags)
    }

    fun setParams(params: Map<String, String>?) {
        if (params != null) {
            for ((key, value) in params) {
                mSpeechSynthesizer!!.setParam(key, value)
            }
        }
    }

    fun pause(): Int {
        return mSpeechSynthesizer!!.pause()
    }

    fun resume(): Int {
        return mSpeechSynthesizer!!.resume()
    }

    fun stop(): Int {
        return mSpeechSynthesizer!!.stop()
    }

    /**
     * 引擎在合成时该方法不能调用！！！
     * 注意 只有 TtsMode.MIX 才可以切换离线发音
     *
     * @return res
     */
    fun loadVoiceModel(modelFilename: String?, textFilename: String?): Int {
        val res = mSpeechSynthesizer!!.loadModel(modelFilename, textFilename)
        sendToUiThread("切换离线发音人成功。")
        return res
    }

    /**
     * 设置播放音量，默认已经是最大声音
     * 0.0f为最小音量，1.0f为最大音量
     *
     * @param leftVolume  [0-1] 默认1.0f
     * @param rightVolume [0-1] 默认1.0f
     */
    fun setStereoVolume(leftVolume: Float, rightVolume: Float) {
        mSpeechSynthesizer!!.setStereoVolume(leftVolume, rightVolume)
    }

    open fun release() {
        mSpeechSynthesizer!!.stop()
        mSpeechSynthesizer!!.release()
        mSpeechSynthesizer = null
        isInited = false
    }


    protected fun sendToUiThread(message: String) {
        Vog.d(this,"合成器： $message")
    }

    companion object {
        private var isInited = false
    }
    //
    //protected void sendToUiThread(int action, String message) {
    //    //if (mainHandler == null) { // 可以不依赖mainHandler
    //    //    return;
    //    //}
    //    //Message errMsg = Message.obtain();
    //    //errMsg.what = action;
    //    //errMsg.obj = message + "\n";
    //    //mainHandler.sendMessage(errMsg);
    //}
}
