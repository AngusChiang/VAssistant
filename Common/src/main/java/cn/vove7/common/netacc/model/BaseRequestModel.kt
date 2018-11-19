package cn.vove7.common.netacc.model

import android.annotation.SuppressLint
import android.provider.Settings
import cn.vassistant.plugininterface.app.GlobalApp
import cn.vove7.common.model.UserInfo
import cn.vove7.common.netacc.tool.SecureHelper.signData
import cn.vove7.common.utils.GsonHelper
import java.io.Serializable

/**
 * # BaseRequestModel
 *
 * @author Administrator
 * 2018/9/16
 */
class BaseRequestModel<T:Any>(var body: T? = null, val arg1: String? = null)
    : Serializable {
    val timestamp = (System.currentTimeMillis() / 1000)
    val userId = UserInfo.getUserId()
    var sign: String = signData(GsonHelper.toJson(body), userId,timestamp)
    val userToken = UserInfo.getUserToken()
    @SuppressLint("MissingPermission")
    val deviceId: String = DEVICE_ID

    companion object {
        @SuppressLint("HardwareIds")
        val DEVICE_ID = Settings.Secure.getString(GlobalApp.APP.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "null"
    }
}