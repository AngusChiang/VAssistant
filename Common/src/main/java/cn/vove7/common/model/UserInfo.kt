package cn.vove7.common.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

/**
 * # UserInfo
 *
 * @author 17719247306
 * 2018/9/11
 */
class UserInfo : Serializable {
    @SerializedName("userId")
    val userId: Long = -1
    @SerializedName("userName")
    var userName: String? = null
    var email: String? = null
    var userPass: String? = null
    val regTime: Date? = null
    val userToken: String? = null

    fun success() {
        INSTANCE = this
        isLogin = true
    }

    fun logout() {
        INSTANCE = null
        isLogin = false
    }

    companion object {
        val userId: Long
            get() = INSTANCE?.userId ?: -1
        var isLogin = false
        val userName: String?
            get() = INSTANCE?.userName
        val email: String?
            get() = INSTANCE?.email
        val regTime: Date?
            get() = INSTANCE?.regTime
        val userToken: String?
            get() = INSTANCE?.userToken

        var INSTANCE: UserInfo? = null

    }
}
