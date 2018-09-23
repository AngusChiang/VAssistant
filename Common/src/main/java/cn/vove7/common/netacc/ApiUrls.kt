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
    private val PAY = SERVER_IP + "p/"
    val LOGIN = ACCOUNT + "lv"
    val ACTIVATE_VIP = ACCOUNT + "act"
    val VERIFY_TOKEN = ACCOUNT + "vt"
    val GET_USER_INFO = ACCOUNT + "gi"
    val GET_PRICES = ACCOUNT + "gp"

    val REGISTER_BY_EMAIL = ACCOUNT + "rbe"
    val SEND_EMAIL_VER_CODE = ACCOUNT + "sec"

    val SYNC_MARKED = MARKED + "sm"
    val SHARE_MARKED = MARKED + "sd"
    val SHARE_APP_AD_INFO = MARKED + "sai"
    val DELETE_SHARE_MARKED = MARKED + "dsm"
    val DELETE_SHARE_APP_AD = MARKED + "dsa"
    val SYNC_APP_AD = MARKED + "saa"


    val SYNC_GLOBAL_INST = ACTION + "sg"
    val SYNC_IN_APP_INST = ACTION + "sia"
    val SHARE_INST = ACTION + "fu"
    val DELETE_SHARE_INST = ACTION + "ud"
    val UPGRADE_INST = ACTION + "us"

    val GET_ALI_ORDER = PAY + "a"

    val USER_GUIDE = SERVER_IP + "guide"
    val HELP_DEL_INST = "$USER_GUIDE#del-inst"
}