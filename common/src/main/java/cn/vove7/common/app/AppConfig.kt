package cn.vove7.common.app

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import cn.vove7.common.BuildConfig
import cn.vove7.common.R
import cn.vove7.common.bridges.SystemBridge
import cn.vove7.common.model.UserInfo
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.net.WrapperNetHelper
import cn.vove7.common.utils.StorageHelper
import cn.vove7.common.utils.ThreadPool.runOnCachePool
import cn.vove7.common.utils.ThreadPool.runOnPool
import cn.vove7.common.utils.runOnUi
import cn.vove7.common.utils.secure.SecuritySharedPreference
import cn.vove7.smartkey.BaseConfig
import cn.vove7.smartkey.android.AndroidSettings
import cn.vove7.smartkey.android.noCacheKey
import cn.vove7.smartkey.android.smartKey
import cn.vove7.smartkey.annotation.Config
import cn.vove7.smartkey.key.get
import cn.vove7.smartkey.key.set
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import com.google.gson.Gson
import com.russhwolf.settings.Settings
import com.umeng.analytics.MobclickAgent
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
@Config(BuildConfig.CONFIG_NAME)
object AppConfig : BaseConfig {
    //key... value

    val panelStyle: Int by noCacheKey(0, R.string.key_panel_style)
    var openAppCompat by noCacheKey(false, keyId = R.string.key_open_app_compat)
    var speechEngineType: Int by noCacheKey(0, keyId = R.string.key_speech_engine_type)

    var vibrateWhenStartRecog by noCacheKey(true, keyId = R.string.key_vibrate_reco_begin)
    var isToastWhenRemoveAd by noCacheKey(true, keyId = R.string.key_show_toast_when_remove_ad)
    var isAdBlockService by noCacheKey(false, keyId = R.string.key_open_ad_block)

    var isLongPressKeyWakeUp by noCacheKey(true, keyId = R.string.key_long_press_volume_up_wake_up)

    var wakeupKeys by smartKey(intArrayOf())

    val voiceControlDialog by noCacheKey(true, keyId = R.string.key_voice_control_dialog)
    val adWaitSecs by noCacheKey(17, keyId = R.string.key_ad_wait_secs)

    //语音唤醒
    var voiceWakeup by noCacheKey(false, keyId = R.string.key_open_voice_wakeup)

    var autoOpenAS by noCacheKey(false, keyId = R.string.key_auto_open_as_with_root)
    var autoSetAssistantApp by noCacheKey(false, keyId = R.string.key_auto_set_assistant_app)
    var userExpPlan by noCacheKey(true, keyId = R.string.key_user_exp_plan)
    var isAutoVoiceWakeupCharging by noCacheKey(false, keyId = R.string.key_auto_open_voice_wakeup_charging)

    var xunfeiSpeechKey by noCacheKey("5d184fe9"/*"5c5437d6"*//*"5d0f2ed4"*/, R.string.key_xunfei_speech_key)

    var useSmartOpenIfParseFailed by noCacheKey(true, keyId = R.string.key_use_smartopen_if_parse_failed)

    // 云服务解析 无用
    var cloudServiceParseIfLocalFailed by noCacheKey(false, keyId = R.string.key_only_cloud_service_parse)

    const val WAKEUP_FILE_NHXV = "assets:///bd/WakeUp_nhxv.bin"
    const val WAKEUP_FILE_XVTX = "assets:///bd/WakeUp_xvtx.bin"

    var DEFAULT_WAKEUP_FILE = WAKEUP_FILE_NHXV

    var openResponseWord by noCacheKey(false, keyId = R.string.key_open_response_word)

    var responseWord by noCacheKey("我在", keyId = R.string.key_response_word)

    var speakResponseWordOnVoiceWakeup by noCacheKey(true, keyId = R.string.key_speak_response_word_on_voice_wakeup)

//    var volumeWakeUpWhenScreenOff by noCacheKey(true, keyId = R.string.key_volume_wakeup_when_screen_off)

    //    var onlyCloudServiceParse = false //云服务解析
    val synStreamIndex: Int
        //合成输出通道 对应 R.array.list_stream_syn_output
        get() = getSingleChoicePosition(
                R.string.key_stream_of_syn_output,
                R.array.list_stream_syn_output, 0
        )

    //音量长按延迟
    var volumeKeyDelayUp by noCacheKey(400, keyId = R.string.key_long_key_press_delay)

    var wakeUpFilePath by noCacheKey(DEFAULT_WAKEUP_FILE, keyId = R.string.key_wakeup_file_path)

    //耳机中键唤醒
    var wakeUpWithHeadsetHook by noCacheKey(false, keyId = R.string.key_wakeup_with_headsethook)

    // 自动休眠后，亮屏自动开启语音唤醒
    var openVoiceWakeUpIfAutoSleep by noCacheKey(true, keyId = R.string.key_open_voice_wakeup_if_auto_sleep)

    var openChatSystem by noCacheKey(true, keyId = R.string.key_open_chat_system)

    //语音识别等待时长 s
    var recogWaitDurationMillis by noCacheKey(1000, keyId = R.string.key_recog_wait_duration)

    val autoSleepWakeupMillis: Long
        get() {
            val oneMinute: Long = 60 * 1000
            return when (getSingleChoicePosition(R.string.key_auto_sleep_wakeup_duration, R.array.list_auto_sleep_duration)) {
                0 -> oneMinute
                1 -> oneMinute * 5
                2 -> oneMinute * 10
                3 -> oneMinute / 30
                4 -> -1 //不休眠
                else -> 10 * oneMinute
            }.also {
                Vog.d("reload ---> autoSleepWakeupMillis = $it")
            }
        }

    val chatSystem: Int
        get() {
            val i = getSingleChoicePosition(R.string.key_chat_system_type, R.array.list_chat_system)
            return (if (!UserInfo.isVip()) 0 else i).also { Vog.d("reload ---> chatSystem $it") }
        }

    var homeSystem: Int? by smartKey(null, R.string.key_home_system)
    var homeSystemConfig: String? by smartKey(null, keyId = R.string.key_home_system_config, encrypt = true)
    //用户自定义短语
    var homeSystemUserCommand: String? by smartKey(null, keyId = R.string.key_home_system_user_command, encrypt = true)


    val fpAnimation: Int
        get() = getSingleChoicePosition(R.string.key_fp_animation, R.array.list_fp_animation, 0)

    val homeFun: Int //长按HOME键功能
        get() = getSingleChoicePosition(R.string.key_home_fun, R.array.list_home_funs)

    //用户唤醒词
    var userWakeupWord by noCacheKey("", R.string.key_user_wakeup_word)

    //    var continuousDialogue = false//连续对话

    //助手服务
    var useAssistService by noCacheKey(true, keyId = R.string.key_use_assist_service)

//    //执行失败语音反馈
//    var execFailedVoiceFeedback by noCacheKey(true, keyId = R.string.key_exec_failed_voice_feedback)
//
//    //执行成功反馈
//    var execSuccessFeedback by noCacheKey(true, keyId = R.string.key_exec_failed_voice_feedback)

    var fixVoiceMicro by noCacheKey(false, keyId = R.string.key_fix_voice_micro)//麦克风冲突

    //通知唤醒状态/麦克风
    var notifyCloseMicro by noCacheKey(true, keyId = R.string.key_close_wakeup_notification)

    val translateLang: String
        //翻译主语言
        get() {
            val i = getSingleChoicePosition(R.string.key_translate_languages, R.array.list_translate_languages, 0)
            return arrayOf("auto", "zh", "en", "yue", "wyw", "jp", "kor", "fra", "spa", "th", "ara", "ru",
                    "pt", "de", "it", "el", "nl", "pl", "bul", "est", "dan", "fin", "cs", "rom", "slo",
                    "swe", "hu", "cht", "vie")[i]
        }

    val voiceRecogEffect by noCacheKey(false, keyId = R.string.key_voice_recog_feedback)

    //语音识别提示音
    val voiceRecogFeedback
        get() = voiceRecogEffect || isBlueToothConnect

    var notifyWpOnScreenOff by noCacheKey(true, keyId = R.string.key_notify_wp_on_screen_off)

    var devMode by noCacheKey(BuildConfig.DEBUG, keyId = R.string.key_dev_mode)

    //fixme 连接手表也为true
    val isBlueToothConnect: Boolean
        get() {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            adapter.bondedDevices?.forEach {
                it.type
            }
            return (BluetoothProfile.STATE_CONNECTED ==
                    adapter.getProfileConnectionState(BluetoothProfile.HEADSET)).also {
                Vog.d("蓝牙连接：$it")
            }

        }

    //长语音 连续命令
    var lastingVoiceCommand by noCacheKey(false, keyId = R.string.key_lasting_voice_command)

    val listeningToastAlignDirection
        //对齐方向
        get() = getSingleChoicePosition(R.string.key_float_voice_align, R.array.list_float_voice_align, 0).also {
            Vog.d("悬浮依靠方向 $it")
        }

    var autoCheckPluginUpdate by noCacheKey(true, keyId = R.string.key_auto_check_plugin_update)

    var FIRST_LAUNCH_NEW_VERSION = false or BuildConfig.DEBUG //新版本第一次启动

    var IS_SYS_APP = false
    var smartKillAd by noCacheKey(false, keyId = R.string.key_smart_find_and_kill_ad) // 跳过自动识别未标记的广告

    //对话系统 字符串
    var chatStr: String? by noCacheKey(null, keyId = R.string.key_chat_str)

    var textOcrStr: String? by noCacheKey(null, keyId = R.string.key_text_ocr_key)

    val streamTypeArray = arrayOf(
            AudioManager.STREAM_MUSIC
            , AudioManager.STREAM_RING
            , AudioManager.STREAM_NOTIFICATION
    )

    val currentStreamType: Int
        get() {
            val i = synStreamIndex.let { if (it in 0..2) it else 0 }
            Vog.d("currentStreamIndex ---> $i")
            return streamTypeArray[i]
        }

    /**
     * 当前合成通道音量
     */
    val currentStreamVolume get() = SystemBridge.getVolumeByType(currentStreamType)

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
        val nowCode = versionCode
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
                Vog.d("init user info ---> $info")
                info.success()//设置登陆后，读取配置  null 抛出空指针
                WrapperNetHelper.postJson<Any>(ApiUrls.VERIFY_TOKEN) { }
            } catch (e: Exception) {
                GlobalLog.err(e)
                GlobalApp.toastError("用户信息提取失败，请重新登陆")
                ssp.remove(context.getString(R.string.key_login_info))
            }
        } else {
            Vog.d("init ---> not login")
        }
    }

    fun login(userInfo: UserInfo) {
        MobclickAgent.onProfileSignIn("${UserInfo.getUserId()}")
        userInfo.success()
        //保存->sp
        val infoJson = Gson().toJson(userInfo)
        ssp.edit().putString(context.getString(R.string.key_login_info), infoJson).apply()
    }

    fun logout() {
        MobclickAgent.onProfileSignOff()
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
                    WrapperNetHelper.postJson<Any>(ApiUrls.CHECK_USER_DATE) { }
                }
            }
        }
    }

    fun checkLogin(): Boolean {
        return if (!UserInfo.isLogin()) {
            GlobalApp.toastInfo(R.string.text_please_login_first)
            false
        } else true
    }

    fun checkUser(): Boolean {
        checkDate()
        if (!UserInfo.isLogin()) {
            GlobalApp.toastInfo(R.string.text_please_login_first)
            return false
        } else if (!UserInfo.isVip()) {
            GlobalApp.toastWarning(R.string.text_need_vip)
            return false
        }
        return true
    }

    //适配单选 存储String/Int
    private fun getSingleChoicePosition(keyId: Int, entityId: Int, def: Int = -1): Int {
        return try {
            return GlobalApp.APP.resources.getStringArray(entityId).indexOf(sp.getString(keyId))
        } catch (e: ClassCastException) {
            settings.getInt(context.getString(keyId), def)
        }
    }

    val sp: SpHelper by lazy { SpHelper(GlobalApp.APP) }

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

    /**
     *
     * @param context Context
     * @param byUser Boolean
     * @param onUpdate Function1<Pair<String, String>?, Unit>?
     */
    fun checkAppUpdate(context: Context, byUser: Boolean, onUpdate: ((Pair<String, String>?) -> Unit)? = null) {
        if (BuildConfig.DEBUG && !byUser) {
            return
        }
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
                        Vog.d("checkAppUpdate ---> 忽略此版")
                        return@runOnUi
                    }
                    if (checkHasUpdate(verName)) {
                        onUpdate?.invoke(verName to log)
                    } else
                        onUpdate?.invoke(null)
                }
            } catch (e: Exception) {
                GlobalLog.err("检查更新失败" + e.message)
                onUpdate?.invoke(null)
            }

        }
    }

    private fun checkHasUpdate(coolVersion: String): Boolean {
        return try {
            version2Int(coolVersion) > version2Int(versionName)
        } catch (e: Exception) {
            coolVersion != versionName
        }
    }

    /**
     * 版本号 -> 整数
     * 规则: 1.1.0
     * 小版本: 1.1.1.1
     *
     * @param s String
     * @return Int
     */
    private fun version2Int(s: String): Float {
        var sum = 0f
        var t = 1000000f
        s.split('.').forEach {
            sum += it.toInt() * t
            t /= 100
        }
        return sum
    }

    fun openCoolapk(context: Context) {
        val coolMarketPkg = "com.coolapk.market"

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=${context.packageName}")
        //跳转酷市场
        if (SystemBridge.hasInstall(coolMarketPkg)) {
            intent.setClassName(coolMarketPkg, "com.coolapk.market.view.app.AppViewV8Activity")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                e.log()
                SystemBridge.openUrl("https://www.coolapk.com/apk/cn.vove7.vassistant")
            }
        } else {
            SystemBridge.openUrl("https://www.coolapk.com/apk/cn.vove7.vassistant")
        }
    }

    /**
     * 文字提取翻译权限
     * @return Boolean
     */
    fun haveTranslatePermission(): Boolean? {
        if (!UserInfo.isLogin()) {
            GlobalApp.toastWarning("使用翻译功能，请先登录")
            return null
        } else if (UserInfo.isVip())
            return true

        //已登陆/检查次数
        val f = getTodayCount("translate_count")
        Vog.d("haveTranslatePermission ---> 翻译次数 $f")
        return if (f < 10) {//免费10次
            plusTodayCount("translate_count", f)
            true
        } else {
            GlobalApp.toastInfo("免费使用次数已用尽")
            null
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
        return (f < 10).also {
            //免费10次
            if (it) runOnCachePool {
                plusTodayCount("tp", f)
            }
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

}

@Suppress("UNCHECKED_CAST")
fun <T> Settings.get(keyId: Int, defaultValue: T?, cls: Class<*>, encrypt: Boolean = false): T? {
    return get(AndroidSettings.s(keyId), defaultValue, cls, encrypt)
}

fun <T> Settings.set(keyId: Int, value: T?, encrypt: Boolean = false) {
    set(AndroidSettings.s(keyId), value, encrypt)
}
