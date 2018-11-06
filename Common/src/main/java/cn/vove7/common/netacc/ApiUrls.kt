package cn.vove7.common.netacc

import cn.vove7.common.BuildConfig

/**
 * # ApiUrls
 *
 * @author 17719247306
 * 2018/9/11
 */
object ApiUrls {
    val debig_server = "http://192.168.137.1:8080/"
    val official_server = "http://115.159.155.25:8080/"

    var SERVER_IP = /*if (BuildConfig.DEBUG) debig_server else*/ official_server

    private val ACCOUNT: String get() = SERVER_IP + "account/"
    private val MARKED: String get() = SERVER_IP + "marked/"
    private val ACTION: String get() = SERVER_IP + "action/"
    private val COMMON: String get() = SERVER_IP + "common/"
    private val APP: String get() = SERVER_IP + "app/"
    private val SERVICE: String get() = SERVER_IP + "service/"

    val CRASH_HANDLER: String get() = COMMON + "ch"
    val GET_IP: String get() = COMMON + "ip"
    val GET_LAST_DATA_DATE: String get() = COMMON + "gldd"

    private val PAY: String get() = SERVER_IP + "p/"
    val LOGIN: String get() = ACCOUNT + "lv"
    val ACTIVATE_VIP: String get() = ACCOUNT + "act"
    val VERIFY_TOKEN: String get() = ACCOUNT + "vt"
    val GET_USER_INFO: String get() = ACCOUNT + "gi"
    val GET_PRICES: String get() = ACCOUNT + "gp"
    val MODIFY_NAME: String get() = ACCOUNT + "mun"
    val MODIFY_PASS: String get() = ACCOUNT + "mup"
    val CHECK_USER_DATE: String get() = ACCOUNT + "cud"

    val REGISTER_BY_EMAIL: String get() = ACCOUNT + "rbe"
    val RET_PASS__BY_EMAIL: String get() = ACCOUNT + "rpbe"
    val SEND_SIGN_UP_EMAIL_VER_CODE: String get() = ACCOUNT + "sec"
    val SEND_RET_PASS_EMAIL_VER_CODE: String get() = ACCOUNT + "srec"

    val SYNC_MARKED: String get() = MARKED + "sm"
    val SHARE_MARKED: String get() = MARKED + "sd"
    val SHARE_APP_AD_INFO: String get() = MARKED + "sai"
    val DELETE_SHARE_MARKED: String get() = MARKED + "dsm"
    val DELETE_SHARE_APP_AD: String get() = MARKED + "dsa"
    val SYNC_APP_AD: String get() = MARKED + "saa"


    val SYNC_GLOBAL_INST: String get() = ACTION + "sg"
    val SYNC_IN_APP_INST: String get() = ACTION + "sia"
    val SHARE_INST: String get() = ACTION + "fu"
    val DELETE_SHARE_INST: String get() = ACTION + "ud"
    val UPGRADE_INST: String get() = ACTION + "us"

    val GET_ALI_ORDER: String get() = PAY + "a"

    const val USER_GUIDE = "https://vove.gitee.io/"
    const val INST_REGEX_GUIDE = "https://vove.gitee.io/"//todo
    const val USER_FAQ = "https://vove.gitee.io/2018/09/24/User_Manual/#faq常见问题"

    val HELP_DEL_INST: String get() = "$USER_GUIDE#del-inst"

    val UPLOAD_CMD_HIS: String get() = APP + "uch"
    val NEW_USER_FEEDBACK: String get() = APP + "ufb"

    val QQ_GROUP_1 = "http://qm.qq.com/cgi-bin/qm/qr?k=BKTXyMMmLDKS8SXOht71bKKbI9rdPAd3"

    val CLOUD_PARSE :String get()= APP + "cp"

    fun switch() {
        SERVER_IP = if (SERVER_IP == debig_server) official_server else debig_server
    }

}