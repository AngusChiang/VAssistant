package cn.vove7.jarvis.tools

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.model.UserInfo
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.utils.StorageHelper
import cn.vove7.common.utils.ThreadPool.runOnCachePool
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.utils.secure.SecuritySharedPreference
import cn.vove7.executorengine.bridges.SystemBridge
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.google.gson.Gson
import org.jsoup.Jsoup
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * # AppConfig
 *
 * @author Administrator
 * 2018/9/16
 */
object AppConfig {
    //key... value
    var vibrateWhenStartReco = true
    var isToastWhenRemoveAd = true
    var isAdBlockService = false
    var isLongPressVolUpWakeUp = true
    var voiceControlDialog = true
    var adWaitSecs = 17
    var voiceWakeup = false
        //语音唤醒
        set(v) {
            field = v
            sp.set(R.string.key_open_voice_wakeup, v)
        }
    var autoOpenASWithRoot = false
    var audioSpeak = true//播报语音
    var userExpPlan = true
    var isAutoVoiceWakeupCharging = false
    var useSmartOpenIfParseFailed = true
    var cloudServiceParseIfLocalFailed = false //云服务解析
    var autoUpdateData = true //
    var WAKEUP_FILE_NHXV = "assets:///bd/WakeUp_nhxv.bin"
    var WAKEUP_FILE_XVTX = "assets:///bd/WakeUp_xvtx.bin"
    var DEFAULT_WAKEUP_FILE = WAKEUP_FILE_NHXV
    var openResponseWord = false
    var responseWord = "我在"
    var speakResponseWordOnVoiceWakeup = true
    var volumeWakeUpWhenScreenOff = true
    //    var onlyCloudServiceParse = false //云服务解析
    var synStreamIndex: Int = 0//合成输出通道 对应 R.array.list_stream_syn_output

    var volumeKeyDelayUp = 600//音量长按延迟
    var wakeUpFilePath = DEFAULT_WAKEUP_FILE
    var wakeUpWithHeadsetHook = false//耳机中键唤醒

    var openVoiceWakeUpIfAutoSleep = true// 自动休眠后，亮屏自动开启语音唤醒
    var openChatSystem = true
    var autoSleepWakeupMillis: Long = 10 * 60 * 1000
    var chatSystem: String = ""
    var userWakeupWord: String = ""//用户唤醒词
    //    var continuousDialogue = false//连续对话
    var finishWord: String? = null
    //    var resumeMusic = true//继续播放
    var recoWhenWakeupAssist = false//立即识别
    var useAssistService = true//助手服务
    var execFailedVoiceFeedback = true//执行失败语音反馈
    var execSuccessFeedback = true//执行成功反馈
    var fixVoiceMico = true//麦克风冲突
    var notifyCloseMico = true//通知唤醒状态/麦克风
    //    var disableAdKillerOnLowBattery = true//低电量关闭去广告
    @Deprecated("无障碍省电模式 弃用")
    var disableAccessibilityOnLowBattery = true//低电量关闭无障碍
    var translateLang = "auto"//翻译主语言
    var voiceRecogFeedback = false //语音识别提示音
    var lastingVoiceCommand = false //长语音 连续命令
    var lastingVoiceMillis: Int = 20 //长语音等待时间 单位秒

    var listeningToastAlignDirection = 0//对齐方向

    var autoCheckPluginUpdate = true

    var FIRST_LAUNCH_NEW_VERSION = false or BuildConfig.DEBUG //新版本第一次启动

    var IS_SYS_APP = false
    var smartKillAd = false // 跳过自动识别未标记的广告

    val streamTypeArray = arrayOf(
            AudioManager.STREAM_MUSIC
            , AudioManager.STREAM_RING
            , AudioManager.STREAM_NOTIFICATION
    )

    val currentStreamType: Int
        get() {
            val i = AppConfig.synStreamIndex.let { if (it in 0..2) it else 0 }
            Vog.d(this, "currentStreamIndex ---> $i")
            return streamTypeArray[i]
        }

    /**
     * 当前合成通道音量
     */
    val currentStreamVolume get() = SystemBridge.getVolumeByType(AppConfig.currentStreamType)

    /**
     * 改变sp存储路径
     * 重新安装
     * todo: 新安装时无存储权限
     */
    fun changeStorePath() {
        val filePath = StorageHelper.spPath
        try {
            var field = ContextWrapper::class.java.getDeclaredField("mBase")
            field.isAccessible = true
            val obj = field.get(GlobalApp.APP)
            field = obj.javaClass.getDeclaredField("mPreferencesDir")
            field.isAccessible = true
            field.set(obj, File(filePath))
        } catch (e: Exception) {
            e.printStackTrace()
            GlobalLog.err("配置存储重定向失败")
        }
    }

    fun init() {
        checkFirstLaunch()
        checkIsSystemApp()
        checkUserInfo()
    }

    /**
     * 启动时执行一次
     */
    private fun checkFirstLaunch() {
        if (BuildConfig.DEBUG) return
        val lastCode = sp.getLong("v_code")
        val nowCode = AppConfig.versionCode
        if (lastCode < nowCode) {
            FIRST_LAUNCH_NEW_VERSION = true
            sp.set("v_code", nowCode)
        } else {
            FIRST_LAUNCH_NEW_VERSION = false
        }
    }

    private fun checkIsSystemApp() {
        val f = context.packageManager.getPackageInfo(context.packageName, 0)
                ?.applicationInfo?.flags ?: 0

        IS_SYS_APP = (f and ApplicationInfo.FLAG_SYSTEM) == 1
        GlobalLog.log("是否系统应用：$IS_SYS_APP")
    }

    val context get() = GlobalApp.APP
    private val ssp: SecuritySharedPreference by lazy { SecuritySharedPreference(context, "xka", Context.MODE_PRIVATE) }
    /**
     * checkUserInfo完reload
     */
    private fun checkUserInfo() {
        //用户信息
        val key = context.getString(R.string.key_login_info)
        if (ssp.contains(key)) {
            try {
                val info = Gson().fromJson(ssp.getString(key, null),
                        UserInfo::class.java)
                Vog.d(this, "init user info ---> $info")
                info.success()//设置登陆后，读取配置  null 抛出空指针
                NetHelper.postJson<Any>(ApiUrls.VERIFY_TOKEN)
            } catch (e: Exception) {
                GlobalLog.err(e)
                GlobalApp.toastShort("用户信息提取失败，请重新登陆")
                ssp.remove(context.getString(R.string.key_login_info))
            }
        } else {
            Vog.d(this, "init ---> not login")
        }
        reload()
    }

    fun login(userInfo: UserInfo) {
        userInfo.success()
        //保存->sp
        val infoJson = Gson().toJson(userInfo)
        ssp.edit().putString(context.getString(R.string.key_login_info), infoJson).apply()
    }

    fun logout() {
        ssp.remove(context.getString(R.string.key_login_info))
        UserInfo.logout()
    }

    var lastCheckTime = 0L
    /**
     * 发送请求，验证时间
     */
    fun checkDate() {
        runOnCachePool {
            if (System.currentTimeMillis() - lastCheckTime > 600000) {
                if (UserInfo.isLogin()) {
                    lastCheckTime = System.currentTimeMillis()
                    NetHelper.postJson<Any>(ApiUrls.CHECK_USER_DATE, BaseRequestModel(""))
                }
            }
        }
    }

    fun checkLogin(): Boolean {
        return if (!UserInfo.isLogin()) {
            GlobalApp.toastShort(R.string.text_please_login_first)
            false
        } else true
    }

    fun checkUser(): Boolean {
        checkDate()
        if (!UserInfo.isLogin()) {
            GlobalApp.toastShort(R.string.text_please_login_first)
            return false
        } else if (!UserInfo.isVip()) {
            GlobalApp.toastShort(R.string.text_need_vip)
            return false
        }
        return true
    }
    //todo map keyId -> value object 直接修改对应值 无需reload全部


    //load
    fun reload() {
        vibrateWhenStartReco = getBooleanAndInit(R.string.key_vibrate_reco_begin, true)
        isToastWhenRemoveAd = getBooleanAndInit(R.string.key_show_toast_when_remove_ad, true)
        isAdBlockService = getBooleanAndInit(R.string.key_open_ad_block, false)
        isLongPressVolUpWakeUp = getBooleanAndInit(R.string.key_long_press_volume_up_wake_up, true)
        voiceControlDialog = getBooleanAndInit(R.string.key_voice_control_dialog, true)
        voiceWakeup = getBooleanAndInit(R.string.key_open_voice_wakeup, false)
        audioSpeak = getBooleanAndInit(R.string.key_audio_speak, true)
        userExpPlan = getBooleanAndInit(R.string.key_user_exp_plan, true)
        isAutoVoiceWakeupCharging = getBooleanAndInit(R.string.key_auto_open_voice_wakeup_charging, false)
        useSmartOpenIfParseFailed = getBooleanAndInit(R.string.key_use_smartopen_if_parse_failed, true)
        openResponseWord = getBooleanAndInit(R.string.key_open_response_word, false)
        speakResponseWordOnVoiceWakeup = getBooleanAndInit(R.string.key_speak_response_word_on_voice_wakeup, true)
        autoOpenASWithRoot = getBooleanAndInit(R.string.key_auto_open_as_with_root, false)
        openChatSystem = getBooleanAndInit(R.string.key_open_chat_system, true)
        openVoiceWakeUpIfAutoSleep = getBooleanAndInit(R.string.key_open_voice_wakeup_if_auto_sleep, true)
//        continuousDialogue = getBooleanAndInit(R.string.key_continuous_dialogue, false)
//  todo      cloudServiceParseIfLocalFailed = getBooleanAndInit(R.string.key_cloud_service_parse, true)
        sp.set(R.string.key_cloud_service_parse, false)
        autoUpdateData = getBooleanAndInit(R.string.key_auto_update_data, true)
//        resumeMusic = getBooleanAndInit(R.string.key_resume_bkg_music, true)
        recoWhenWakeupAssist = getBooleanAndInit(R.string.key_reco_when_wakeup_assist, false)
        volumeWakeUpWhenScreenOff = getBooleanAndInit(R.string.key_volume_wakeup_when_screen_off, true)
        useAssistService = getBooleanAndInit(R.string.key_use_assist_service, useAssistService)
        execFailedVoiceFeedback = getBooleanAndInit(R.string.key_exec_failed_voice_feedback, true)
        execSuccessFeedback = getBooleanAndInit(R.string.key_exec_failed_voice_feedback, true)
        fixVoiceMico = getBooleanAndInit(R.string.key_fix_voice_micro, true) && !AppConfig.IS_SYS_APP
        notifyCloseMico = getBooleanAndInit(R.string.key_close_wakeup_notification, true)
//        disableAdKillerOnLowBattery = getBooleanAndInit(R.string.key_remove_ad_power_saving_mode, true)
        disableAccessibilityOnLowBattery = getBooleanAndInit(R.string.key_accessibility_service_power_saving_mode, true)
        wakeUpWithHeadsetHook = getBooleanAndInit(R.string.key_wakeup_with_headsethook, wakeUpWithHeadsetHook)
        voiceRecogFeedback = getBooleanAndInit(R.string.key_voice_recog_feedback, voiceRecogFeedback)
        lastingVoiceCommand = getBooleanAndInit(R.string.key_lasting_voice_command, lastingVoiceCommand)
        autoCheckPluginUpdate = getBooleanAndInit(R.string.key_auto_check_plugin_update, autoCheckPluginUpdate)
        smartKillAd = getBooleanAndInit(R.string.key_smart_find_and_kill_ad, smartKillAd)

        sp.getInt(R.string.key_lasting_voice_millis).also {
            lastingVoiceMillis = if (it < 0) lastingVoiceMillis else it
        }
        finishWord = sp.getString(R.string.key_finish_word)
//        onlyCloudServiceParse = getBooleanAndInit(R.string.key_only_cloud_service_parse, false)
        userWakeupWord = sp.getString(R.string.key_user_wakeup_word) ?: ""
        synStreamIndex = sp.getString(R.string.key_stream_of_syn_output).let {
            Vog.d(this, "reload ---> $it")
            if (it == null) 0
            else {
                var i = GlobalApp.APP.resources.getStringArray(R.array.list_stream_syn_output).indexOf(it)
                if (i < 0) i = 0
                i
            }
        }
        chatSystem = sp.getString(R.string.key_chat_system_type).let {
            val i = GlobalApp.APP.resources.getStringArray(R.array.list_chat_system)
            if (!UserInfo.isVip()) i[0] else it ?: i[0]
        }.also { Vog.d(this, "reload ---> chatSystem $it") }
        autoSleepWakeupMillis = sp.getString(R.string.key_auto_sleep_wakeup_duration).let {
            if (it == null) autoSleepWakeupMillis
            else {
                val oneHour: Long = 60 * 60 * 1000
                val i = GlobalApp.APP.resources.getStringArray(R.array.list_auto_sleep_duration).indexOf(it)
                when (i) {
                    0 -> oneHour / 6
                    1 -> oneHour / 3
                    2 -> oneHour / 2
                    3 -> oneHour
                    4 -> 2 * oneHour
                    5 -> 5 * oneHour
                    else -> autoSleepWakeupMillis
                }
            }
        }.also {
            Vog.d(this, "reload ---> autoSleepWakeupMillis = $it")
        }

        listeningToastAlignDirection = sp.getString(R.string.key_float_voice_align).let {
            if (it == null)
                0
            else {
                val i = GlobalApp.APP.resources.getStringArray(R.array.list_float_voice_align).indexOf(it)
                if (i < 0) 0
                else i
            }
        }.also {
            Vog.d(this, "悬浮依靠方向 $it")
        }

        responseWord = sp.getString(R.string.key_response_word) ?: responseWord
        translateLang = sp.getString(R.string.key_translate_languages)?.let {
            val i = GlobalApp.APP.resources.getStringArray(R.array.list_translate_languages).indexOf(it)
            if (i == -1) translateLang
            arrayOf("auto", "zh", "en", "yue", "wyw", "jp", "kor", "fra", "spa", "th", "ara", "ru",
                    "pt", "de", "it", "el", "nl", "pl", "bul", "est", "dan", "fin", "cs", "rom", "slo",
                    "swe", "hu", "cht", "vie")[i]
        } ?: translateLang
        wakeUpFilePath = sp.getString(R.string.key_wakeup_file_path) ?: wakeUpFilePath
        sp.getInt(R.string.key_ad_wait_secs).also {
            adWaitSecs = if (it == -1) 17 else it
        }
        sp.getInt(R.string.key_long_key_press_delay).also {
            volumeKeyDelayUp = if (it == -1) volumeKeyDelayUp else it
        }

        Vog.d(this, "reload ---> AppConfig")
    }

    val sp: SpHelper by lazy { SpHelper(GlobalApp.APP) }
    private fun getBooleanAndInit(keyId: Int, default: Boolean = false): Boolean {
        return if (sp.containsKey(keyId)) {
            sp.getBoolean(keyId)
        } else {
            sp.set(keyId, default)
            default
        }
    }

    val versionName: String
        get() {
            return GlobalApp.APP.let {
                it.packageManager.getPackageInfo(
                        it.packageName, 0).versionName
            }
        }
    val versionCode: Long
        get() {
            return GlobalApp.APP.packageManager.getPackageInfo(
                    GlobalApp.APP.packageName, 0).let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    it.longVersionCode
                } else {
                    it.versionCode.toLong()
                }
            }
        }

    fun checkAppUpdate(context: Activity, byUser: Boolean, onUpdate: ((Boolean) -> Unit)? = null) {
//        if (BuildConfig.DEBUG) {
//            return
//        }
        runOnPool {
            try {
                val doc = Jsoup.connect("https://www.coolapk.com/apk/cn.vove7.vassistant")
                        .timeout(5000).get()
                val sp = SpHelper(context)
                val verName = doc.body().getElementsByClass("list_app_info").text()
                val log = doc.body().getElementsByClass("apk_left_title_info")[0]
                        .html().replace("<br> ", "\n")
                runOnUi {
                    val noUpdateName = sp.getString("no_update_ver_name") ?: ""
                    if (!byUser && noUpdateName == verName) {
                        Vog.d(this, "checkAppUpdate ---> 忽略此版")
                        return@runOnUi
                    }
                    if (verName != AppConfig.versionName) {
                        onUpdate?.invoke(true)
                        if (!context.isFinishing) {
                            MaterialDialog(context).title(text = "发现新版本 v$verName")
                                    .message(text = log)
                                    .positiveButton(text = "用酷安下载") { _ ->
                                        openCollapk(context)
                                    }
                                    .checkBoxPrompt(text = "不再提醒此版本") {
                                        if (it) {
                                            sp.set("no_update_ver_name", verName)
                                        } else sp.removeKey("no_update_ver_name")
                                    }
                                    .negativeButton()
                                    .cancelable(false)
                                    .show()
                        }
                    } else
                        onUpdate?.invoke(false)
                }
            } catch (e: Exception) {
                GlobalLog.err("检查更新失败" + e.message)
                onUpdate?.invoke(false)
            }

        }
    }

    val PACKAGE_COOL_MARKET = "com.coolapk.market"
//    //小米应用商店
//    val PACKAGE_MI_MARKET = "com.xiaomi.market"
//    //应用宝
//    val PACKAGE_TENCENT_MARKET = "com.tencent.android.qqdownloader"
//    //豌豆荚
//    val PACKAGE_WANDOUJIA_MARKET = "com.wandoujia.phoenix2"

    private fun openCollapk(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=${context.packageName}")
        //跳转酷市场
        intent.setClassName(PACKAGE_COOL_MARKET, "com.coolapk.market.view.app.AppViewV8Activity")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            GlobalApp.toastShort("未安装酷安")
        }
    }

    /**
     * 文字提取翻译权限
     * @return Boolean
     */
    fun haveTranslatePermission(): Boolean {
        if (!UserInfo.isLogin()) {
            GlobalApp.toastShort("使用翻译功能，请先登录")
            return false
        } else if (UserInfo.isVip())
            return true

        //已登陆/检查次数
        val f = getTodayCount("translate_count")
        Vog.d(this, "haveTranslatePermission ---> 翻译次数 $f")
        return if (f < 5) {//免费10次
            plusTodayCount("translate_count", f)
            true
        } else {
            GlobalApp.toastShort("免费使用次数已用尽")
            false
        }
    }

    /**
     * 去广告次数
     * @return Boolean
     */
    fun haveAdKillSurplus(): Boolean {
        if (UserInfo.isVip())
            return true
        val f = getTodayCount("akc")
        return f < 5
    }

    fun plusAdKillCount() {
        runOnCachePool {
            plusTodayCount("akc")
        }
    }

    /**
     * 文字提取
     * @return Boolean
     */
    fun haveTextPickPermission(): Boolean {
        if (UserInfo.isVip())
            return true
        val f = getTodayCount("tp")
        return if (f < 5) {//免费5次
            runOnCachePool {
                plusTodayCount("tp", f)
            }
            true
        } else {
            GlobalApp.toastShort("免费使用次数已用尽")
            false
        }
    }

    /**
     * 使用sp记录每天..的次数，不保留历史纪录
     * 格式  key  ->  yyyyMMdd|count
     * @param key String
     */
    private fun getTodayCount(key: String): Int {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        try {
            ssp.getString(key, null)?.split("|")?.apply {
                return if (this[0] == today) {
                    this[1].toInt()
                } else 0
            }
        } catch (e: Exception) {
            GlobalLog.err(e)
            return 0
        }
        return 0
    }

    private fun plusTodayCount(key: String, d: Int? = null) {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val v = (d ?: getTodayCount(key)) + 1
        ssp.edit().putString(key, "$today|$v").apply()
    }

    fun a() {

    }
}
