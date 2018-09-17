package cn.vove7.common.netacc

import cn.vove7.common.BuildConfig

/**
 * # ApiUrls
 *
 * @author 17719247306
 * 2018/9/11
 */
object ApiUrls {
    val SERVER_IP = if (BuildConfig.DEBUG) "http://192.168.137.1:8080/"
    else "http://115.159.155.25:8080/"

    private val ACCOUNT = SERVER_IP + "account/"
    private val MARKED = SERVER_IP + "marked/"
    private val ACTION = SERVER_IP + "action/"
    val LOGIN = ACCOUNT + "loginVerify"

    val REGISTER_BY_EMAIL = ACCOUNT + "registerByEmail"

    val SEND_EMAIL_VER_CODE = ACCOUNT + "sendEmailCode"

    val SYNC_MARKED = MARKED + "sync"
    val SYNC_APP_AD = MARKED + "syncAppAd"

    val SYNC_GLOBAL_INST = ACTION + "syncGlobal"


}