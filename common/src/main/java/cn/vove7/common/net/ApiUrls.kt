package cn.vove7.common.net

/**
 * # ApiUrls
 *
 * @author Vove
 * 2018/9/11
 */
object ApiUrls {
    private const val DEBUG_SERVER = "http://192.168.137.1:8080/"
    private const val OFFICIAL_SERVER = "http://47.96.87.117:8080/"

    var SERVER_IP = /*if (BuildConfig.DEBUG) DEBUG_SERVER else*/ OFFICIAL_SERVER

    val anotherIp get() = if (SERVER_IP == DEBUG_SERVER) OFFICIAL_SERVER else DEBUG_SERVER

    private const val ACCOUNT: String = "account/"
    private const val MARKED: String = "marked/"
    private const val ACTION: String = "action/"
    private const val COMMON: String = "common/"
    private const val APP: String = "app/"

    private val SERVICE: String = SERVER_IP + "service/"

    const val CRASH_HANDLER: String = COMMON + "ch"
    const val GET_IP: String = COMMON + "ip"

    const val GET_LAST_DATA_DATE: String = COMMON + "gldd"

    private val PAY: String = SERVER_IP + "p/"
    const val LOGIN: String = ACCOUNT + "lv"
    const val ACTIVATE_VIP: String = ACCOUNT + "act"
    const val VERIFY_TOKEN: String = ACCOUNT + "vt"
    const val GET_USER_INFO: String = ACCOUNT + "gi"
    const val GET_PRICES: String = ACCOUNT + "gp"
    const val MODIFY_NAME: String = ACCOUNT + "mun"
    const val MODIFY_PASS: String = ACCOUNT + "mup"
    const val MODIFY_MAIL: String = ACCOUNT + "mue"
    const val CHECK_USER_DATE: String = ACCOUNT + "cud"

    const val REGISTER_BY_EMAIL: String = ACCOUNT + "rbe"
    const val RET_PASS_BY_EMAIL: String = ACCOUNT + "rpbe"
    const val SEND_SIGN_UP_EMAIL_VER_CODE: String = ACCOUNT + "sec"
    const val SEND_RET_PASS_EMAIL_VER_CODE: String = ACCOUNT + "srec"

    const val SYNC_MARKED: String = MARKED + "sm"
    const val SHARE_MARKED: String = MARKED + "sd"
    const val SHARE_APP_AD_INFO: String = MARKED + "sai"
    const val DELETE_SHARE_MARKED: String = MARKED + "dsm"
    const val DELETE_SHARE_APP_AD: String = MARKED + "dsa"
    const val SYNC_APP_AD: String = MARKED + "saa"

    const val SYNC_GLOBAL_INST: String = ACTION + "sg"
    const val SYNC_IN_APP_INST: String = ACTION + "sia"
    const val SHARE_INST: String = ACTION + "fu"
    const val DELETE_SHARE_INST: String = ACTION + "ud"
    const val UPGRADE_INST: String = ACTION + "us"

    const val USER_GUIDE = "https://vove.gitee.io/"
    const val INST_REGEX_GUIDE = "https://vove.gitee.io/2019/01/29/Customize_Instruction_Regex//"

    const val UPLOAD_CMD_HIS: String = APP + "uch"
    const val CLOUD_PARSE: String = APP + "cp"

    val QQ_GROUP_1 = "http://qm.qq.com/cgi-bin/qm/qr?k=BKTXyMMmLDKS8SXOht71bKKbI9rdPAd3"


}