package cn.vove7.jarvis.utils

import android.os.Looper
import android.widget.Toast
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.model.UserInfo
import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.jarvis.R
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.sharedpreference.SpHelper
import com.google.gson.Gson
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import java.io.Serializable
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
    var adWaitSecs = 17

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
            NetHelper.postJson<Any>(ApiUrls.VERIFY_TOKEN, BaseRequestModel<Serializable>()) { _, bean ->
                if (bean != null) {
                    if (!bean.isOk()) {
                        logout()
                        GlobalApp.toastShort(bean.message)
                    }
                } else {
                    GlobalApp.toastShort("用户信息获取失败")
                }
            }
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

    fun checkUser():Boolean {
        if (!UserInfo.isLogin()) {
            GlobalApp.toastShort(R.string.text_please_login_first)
            return false
        }else if(!UserInfo.isVip()){
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
        sp.getInt(R.string.key_ad_wait_secs).also {
            adWaitSecs = if (it == -1) 17 else it
        }

        Vog.d(this, "reload ---> AppConfig")
    }

    override fun toString(): String {

        return "\nvibrateWhenStartReco: $vibrateWhenStartReco"
    }
}

//
fun SecuredPreferenceStore.Editor.remove(i: Int) = remove(GlobalApp.getString(i)).apply()

fun SecuredPreferenceStore.contains(i: Int): Boolean = contains(GlobalApp.getString(i))
fun SecuredPreferenceStore.getString(i: Int): String? = getString(GlobalApp.getString(i), null)