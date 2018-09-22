package cn.vove7.common.netacc.model

import android.annotation.SuppressLint
import android.provider.Settings
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.model.UserInfo
import cn.vove7.common.netacc.tool.SecureHelper.MD5
import java.io.Serializable

/**
 * # BaseRequestModel
 *
 * @author Administrator
 * 2018/9/16
 */
const val SECRET_KEY = "vove777"
class BaseRequestModel<T : Serializable>(var body: T? = null, val arg1: String? = null)
    : Serializable {
    val timestamp = ((System.currentTimeMillis() / 1000).toInt()).toString()
    var sign: String = MD5(timestamp + SECRET_KEY)
    val userId = UserInfo.getUserId()
    val userToken = UserInfo.getUserToken()
    @SuppressLint("MissingPermission")
    val deviceId: String = DEVICE_ID

    companion object {
        @SuppressLint("HardwareIds")
        val DEVICE_ID = Settings.Secure.getString(GlobalApp.APP.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "null"
    }
}