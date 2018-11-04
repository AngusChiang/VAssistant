package cn.vove7.jarvis.tools

import android.content.Context
import android.os.Build
import android.os.Looper
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.model.UserInfo
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.NetHelper
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.utils.runOnUi
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import org.jsoup.Jsoup
import kotlin.concurrent.thread
import android.content.Intent
import android.net.Uri
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt


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
    var openResponseWord = true
    var responseWord = "我在"
    var speakResponseWordOnVoiceWakeup = true
    var volumeWakeUpWhenScreenOff = true
    //    var onlyCloudServiceParse = false //云服务解析
    var synStreamIndex: Int = 0//合成输出通道 对应 R.array.list_stream_syn_output

    var volumeKeyDelayUp = 600L//音量长按延迟
    var wakeUpFilePath = DEFAULT_WAKEUP_FILE

    var openVoiceWakeUpIfAutoSleep = true// 自动休眠后，亮屏自动开启语音唤醒
    var openChatSystem = true
    var autoSleepWakeupMillis: Long = 30 * 60 * 1000
    var chatSystem: String = ""
    var continuousDialogue = false//连续对话
    var finishWord: String? = null
    //    var resumeMusic = true//继续播放
    var recoWhenWakeupAssist = false//立即识别
    var useAssistService = false//助手服务
    var execFailedVoiceFeedback = true//执行失败语音反馈
    var execSuccessFeedback = true//执行成功反馈
    fun init() {
        thread {
            checkUserInfo()
            reload()
        }
    }

    private fun checkUserInfo() {
        val sp = SecuredPreferenceStore.getSharedInstance()
        //用户信息
        if (sp.contains(R.string.key_login_info)) {
            val info = Gson().fromJson(sp.getString(R.string.key_login_info),
                    UserInfo::class.java)
            Vog.d(this, "init user info ---> $info")

            info.success()
            Looper.prepare()
            NetHelper.postJson<Any>(ApiUrls.VERIFY_TOKEN)
        } else {
            Vog.d(this, "init ---> not login")
        }
    }

    fun login(userInfo: UserInfo) {
        userInfo.success()
        //保存->sp
        val infoJson = Gson().toJson(userInfo)
        val ssp = SecuredPreferenceStore.getSharedInstance()
        ssp.edit().putString(GlobalApp.getString(R.string.key_login_info), infoJson).apply()
    }

    fun logout() {
        SecuredPreferenceStore.getSharedInstance().Editor().remove(R.string.key_login_info)
        UserInfo.logout()
    }

    var lastCheckTime = 0L

    fun checkDate() {
        thread {
            if (System.currentTimeMillis() - lastCheckTime > 600000) {
                if (UserInfo.isLogin()) {
                    lastCheckTime = System.currentTimeMillis()
                    NetHelper.postJson<Any>(ApiUrls.CHECK_USER_DATE, BaseRequestModel(""))
                }
            }
        }
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
        openResponseWord = getBooleanAndInit(R.string.key_open_response_word, true)
        speakResponseWordOnVoiceWakeup = getBooleanAndInit(R.string.key_speak_response_word_on_voice_wakeup, true)
        autoOpenASWithRoot = getBooleanAndInit(R.string.key_auto_open_as_with_root, false)
        openChatSystem = getBooleanAndInit(R.string.key_open_chat_system, true)
        openVoiceWakeUpIfAutoSleep = getBooleanAndInit(R.string.key_open_voice_wakeup_if_auto_sleep, true)
        continuousDialogue = getBooleanAndInit(R.string.key_continuous_dialogue, false)
//  todo      cloudServiceParseIfLocalFailed = getBooleanAndInit(R.string.key_cloud_service_parse, true)
        sp.set(R.string.key_cloud_service_parse, false)
        autoUpdateData = getBooleanAndInit(R.string.key_auto_update_data, true)
//        resumeMusic = getBooleanAndInit(R.string.key_resume_bkg_music, true)
        recoWhenWakeupAssist = getBooleanAndInit(R.string.key_reco_when_wakeup_assist, false)
        volumeWakeUpWhenScreenOff = getBooleanAndInit(R.string.key_volume_wakeup_when_screen_off, true)
        useAssistService = getBooleanAndInit(R.string.key_use_assist_service, useAssistService)
        execFailedVoiceFeedback = getBooleanAndInit(R.string.key_exec_failed_voice_feedback, true)
        execSuccessFeedback = getBooleanAndInit(R.string.key_exec_failed_voice_feedback, true)
        finishWord = sp.getString(R.string.key_finish_word)
//        onlyCloudServiceParse = getBooleanAndInit(R.string.key_only_cloud_service_parse, false)

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
                val i = GlobalApp.APP.resources.getStringArray(R.array.list_auto_sleep_duration).indexOf(it)
                when (i) {
                    0 -> 30 * 60 * 1000
                    1 -> 60 * 60 * 1000
                    2 -> 2 * 60 * 60 * 1000
                    3 -> 5 * 60 * 60 * 1000
                    else -> autoSleepWakeupMillis
                }
            }
        }.also {
            Vog.d(this, "reload ---> autoSleepWakeupMillis = $it")
        }

        responseWord = sp.getString(R.string.key_response_word) ?: responseWord
        wakeUpFilePath = sp.getString(R.string.key_wakeup_file_path) ?: wakeUpFilePath
        sp.getInt(R.string.key_ad_wait_secs).also {
            adWaitSecs = if (it == -1) 17 else it
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

    fun checkAppUpdate(context: Context, byUser: Boolean, onUpdate: ((Boolean) -> Unit)? = null) {
//        if (BuildConfig.DEBUG) {
//            return
//        }
        thread {
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
}

//
fun SecuredPreferenceStore.Editor.remove(i: Int) = remove(GlobalApp.getString(i)).apply()

fun SecuredPreferenceStore.contains(i: Int): Boolean = contains(GlobalApp.getString(i))
fun SecuredPreferenceStore.getString(i: Int): String? = getString(GlobalApp.getString(i), null)