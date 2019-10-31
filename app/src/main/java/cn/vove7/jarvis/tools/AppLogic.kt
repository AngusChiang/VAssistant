package cn.vove7.jarvis.tools

import android.content.Context
import cn.vove7.common.R
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.model.UserInfo
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.net.WrapperNetHelper
import cn.vove7.common.utils.secure.SecuritySharedPreference
import cn.vove7.vtp.log.Vog
import com.google.gson.Gson
import com.umeng.analytics.MobclickAgent

/**
 * # Logic
 * 负责App内逻辑
 * @author Vove
 * 2019/7/1
 */
object AppLogic {

    //启动初始化逻辑
    fun onLaunch() {
        checkUserInfo()
    }

    private val context get() = GlobalApp.APP

    /**
     * checkUserInfo完reload
     */
    private fun checkUserInfo() {
        //用户信息
        val ssp = SecuritySharedPreference(context, "xka", Context.MODE_PRIVATE)
        val key = context.getString(R.string.key_login_info)
        if (ssp.contains(key)) {
            try {
                val info = Gson().fromJson(ssp.getString(key, null),
                        UserInfo::class.java)
                Vog.d("init user info ---> $info")
                info.success()//设置登陆后，读取配置  null 抛出空指针
                WrapperNetHelper.postJson<Any>(ApiUrls.VERIFY_TOKEN) {
                    success { _, responseMessage ->
                        responseMessage.isOk()
                    }
                }
            } catch (e: Exception) {
                GlobalLog.err(e)
                GlobalApp.toastError("用户信息提取失败，请重新登陆")
                ssp.remove(context.getString(R.string.key_login_info))
            }
        } else {
            Vog.d("init ---> not login")
        }
    }

    fun onLogin(userInfo: UserInfo) {
        val ssp = SecuritySharedPreference(context, "xka", Context.MODE_PRIVATE)
        MobclickAgent.onProfileSignIn("${UserInfo.getUserId()}")
        userInfo.success()
        //保存->sp
        val infoJson = Gson().toJson(userInfo)
        ssp.edit().putString(context.getString(R.string.key_login_info), infoJson).apply()
    }

    fun onLogout() {
        val ssp = SecuritySharedPreference(context, "xka", Context.MODE_PRIVATE)
        MobclickAgent.onProfileSignOff()
        ssp.remove(context.getString(R.string.key_login_info))
        UserInfo.logout()
    }

    /**
     * 是否可以使用讯飞
     * @return Boolean
     */
    fun canXunfei(): Boolean {
        return UserInfo.isPermanentVip()
    }

}