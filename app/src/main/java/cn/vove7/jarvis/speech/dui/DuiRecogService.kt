package cn.vove7.jarvis.speech.dui

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import cn.vove7.android.common.logi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.jarvis.speech.RecogEvent
import cn.vove7.jarvis.speech.RecogEvent.Companion.CODE_NO_RESULT
import cn.vove7.jarvis.speech.SpeechRecogService
import cn.vove7.jarvis.speech.WakeupI
import cn.vove7.jarvis.speech.baiduspeech.wakeup.BaiduVoiceWakeup
import cn.vove7.jarvis.speech.baiduspeech.wakeup.RecogWakeupListener
import cn.vove7.jarvis.speech.baiduspeech.wakeup.WakeupEventAdapter
import cn.vove7.vtp.net.toJson
import com.aispeech.AIError
import com.aispeech.AIResult
import com.aispeech.DUILiteConfig
import com.aispeech.DUILiteSDK
import com.aispeech.common.AIConstant
import com.aispeech.common.JSONResultParser
import com.aispeech.export.config.AICloudASRConfig
import com.aispeech.export.engines2.AICloudASREngine
import com.aispeech.export.intent.AICloudASRIntent
import com.aispeech.export.listeners.AIASRListener
import com.aispeech.gourd.EncodeCallback
import com.aispeech.gourd.Gourd
import com.aispeech.gourd.InitParams
import de.robv.android.xposed.DexposedBridge
import de.robv.android.xposed.XC_MethodHook
import io.michaelrocks.paranoid.Obfuscate

/**
 * # DuiRecogService
 *
 * @author Vove
 * @date 2021/9/12
 */
@Obfuscate
class DuiRecogService(event: RecogEvent) : SpeechRecogService(event), AIASRListener {
    override val enableOffline: Boolean
        get() = false

    private val unhook: List<XC_MethodHook.Unhook> = kotlin.runCatching {
        listOf(DexposedBridge.findAndHookMethod(Gourd::class.java, "init",
            Context::class.java,
            InitParams::class.java,
            EncodeCallback::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    param.result = null
                }
            }),
            DexposedBridge.findAndHookMethod(
                TelephonyManager::class.java,
                "getDeviceId",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.throwable = null
                        param.result = "78A63B90A059C4"
                    }
                }
            ))
    }.fold({ it }, { emptyList() })

    private val engine by lazy {
        val config = AICloudASRConfig()
        config.isLocalVadEnable = true
        config.vadResource = "vad_aihome_v0.11.bin"
        AICloudASREngine.createInstance().also {
            it.init(config, this)
        }
    }

    init {
        // 产品认证需设置 apiKey, productId, productKey, productSecret
        val config = DUILiteConfig(
            "d3c265662929841215092b415c257bd6",
            "278578021",
            "cfbdf9df02d199a602ed6f666a2494a3",
            "b06adce105c97ba926da3f18365a69f3")
        config.authTimeout = 5000 //设置授权连接超时时长，默认5000ms
        config.deviceProfileDirPath = GlobalApp.APP.filesDir.absolutePath

        DUILiteSDK.setDebugMode(Log.VERBOSE)
        DUILiteSDK.init(DuiFakeContext(GlobalApp.APP), config, object : DUILiteSDK.InitListener {
            override fun success() {
                "dui auth succ".logi()
                engine.stop()
            }

            override fun error(p0: String?, p1: String?) {
                GlobalLog.err("dui init error: $p0 $p1")
            }
        })
    }

    override val wakeupI: WakeupI by lazy {
        BaiduVoiceWakeup(WakeupEventAdapter(RecogWakeupListener(handler)))
    }

    var lastText = ""
    override fun doStartRecog(silent: Boolean) {
        val aiCloudASRIntent = AICloudASRIntent()
        aiCloudASRIntent.isRealback = true
        aiCloudASRIntent.resourceType = "custom"
        aiCloudASRIntent.lmId = "custom-lm-id"
        aiCloudASRIntent.localVadEnable = false
        lastText = ""
        engine.start(aiCloudASRIntent)
    }

    override fun doStopRecog() {
        engine.stop()
    }

    override fun doRelease() {
        engine.destroy()
        unhook.forEach(XC_MethodHook.Unhook::unhook)
    }

    override fun doCancelRecog() {
        engine.cancel()
    }


    override fun onInit(p0: Int) {
        "onInit $p0".logi()
    }

    override fun onError(p0: AIError?) {
        "onError $p0".logi()
        GlobalLog.err(p0?.toString())
        handler.sendError(p0?.errId ?: -1)
    }

    override fun onResults(results: AIResult?) {
        "onResults :${results?.toJson()}".logi()
        results ?: return
        if (results.resultType == AIConstant.AIENGINE_MESSAGE_TYPE_JSON) {
            val parser = JSONResultParser(results.getResultObject() as String)
            val text = parser.text.replace(" ", "")
            if (parser.`var`.isNotEmpty()) {
                lastText = parser.`var`
                handler.sendTemp(lastText)
            } else if (isListening && text.isNotBlank()) {
                lastText = text
                handler.sendResult(text)
                doCancelRecog()
                stopRecog(false)
            } else if(parser.eof == 1) {
                doCancelRecog()
                stopRecog(false)
                handler.sendError(CODE_NO_RESULT)
            }
        }
    }

    override fun onRmsChanged(p0: Float) {
        "onRmsChanged".logi()
    }

    override fun onReadyForSpeech() {
        "onReadyForSpeech".logi()
        handler.sendReady()
    }

    override fun onBeginningOfSpeech() {
        "onBeginningOfSpeech".logi()
    }

    override fun onEndOfSpeech() {
        "onEndOfSpeech".logi()
        handler.sendFinished()
    }

    override fun onRawDataReceived(p0: ByteArray?, p1: Int) {
    }

    override fun onResultDataReceived(p0: ByteArray?, p1: Int) {
    }

    override fun onNotOneShot() {
    }
}