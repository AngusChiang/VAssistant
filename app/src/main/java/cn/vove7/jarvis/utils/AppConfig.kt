package cn.vove7.jarvis.utils

import android.os.Build
import android.os.Looper
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.model.UserInfo
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import com.google.gson.Gson
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import kotlin.concurrent.thread

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
    var audioSpeak = true//播报语音
    var userExpPlan = true
    var isAutoVoiceWakeupCharging = false
    var useSmartOpenIfParseFailed = true
    var cloudServiceParse = true //云服务解析

    fun init() {
        thread {
            reload()
            checkUserInfo()
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
        ssp.edit().putString(GlobalApp.getString(R.string.key_login_info), infoJson)
                .apply()
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
        val sp = SpHelper(GlobalApp.APP)
        vibrateWhenStartReco = sp.getBoolean(R.string.key_vibrate_reco_begin, true)
        isToastWhenRemoveAd = sp.getBoolean(R.string.key_show_toast_when_remove_ad, true)
        isAdBlockService = sp.getBoolean(R.string.key_open_ad_block, false)
        isLongPressVolUpWakeUp = sp.getBoolean(R.string.key_long_press_volume_up_wake_up, true)
        voiceControlDialog = sp.getBoolean(R.string.key_voice_control_dialog, true)
        voiceWakeup = sp.getBoolean(R.string.key_open_voice_wakeup, false)
        audioSpeak = sp.getBoolean(R.string.key_audio_speak, true)
        userExpPlan = sp.getBoolean(R.string.key_user_exp_plan, true)
        isAutoVoiceWakeupCharging = sp.getBoolean(R.string.key_auto_open_voice_wakeup_charging, false)
        useSmartOpenIfParseFailed = sp.getBoolean(R.string.key_use_smartopen_if_parse_failed, true)
        cloudServiceParse = sp.getBoolean(R.string.key_cloud_service_parse, true)
        sp.getInt(R.string.key_ad_wait_secs).also {
            adWaitSecs = if (it == -1) 17 else it
        }

        Vog.d(this, "reload ---> AppConfig")
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
}

//
fun SecuredPreferenceStore.Editor.remove(i: Int) = remove(GlobalApp.getString(i)).apply()

fun SecuredPreferenceStore.contains(i: Int): Boolean = contains(GlobalApp.getString(i))
fun SecuredPreferenceStore.getString(i: Int): String? = getString(GlobalApp.getString(i), null)