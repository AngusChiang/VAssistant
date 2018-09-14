package cn.vove7.common.model

import java.util.*

/**
 * # UserInfo
 *
 * @author 17719247306
 * 2018/9/11
 */
class UserInfo {
    val userId: Long = -1
    val userName: String? = null
    val email: String? = null
//    val userPass: String? = null
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
