package cn.vove7.common.net.model

import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.appbus.AppBus.EVENT_FORCE_OFFLINE

/**
 * User: Vove
 * Date: 2018/7/11
 * Time: 22:35
 */
open class ResponseMessage<T> {
    var code: Int = -1

    var message: String = "null"

    var err: String? = null
    var data: T? = null

    fun isOk(): Boolean {
        if (tokenIsOutdate()||isInvalid()) {
            GlobalApp.toastError(message)
            AppConfig.logout()
            AppBus.post(EVENT_FORCE_OFFLINE)
        }
        return code == CODE_OK
    }

    fun isInvalid(): Boolean {
        return code == CODE_INVALID
    }

    fun tokenIsOutdate(): Boolean {
        return code == CODE_TOKEN_OUT_DATE
    }


    override fun toString(): String {
        return "{code=$code, message=$message, err=$err, data=$data}"
    }


    constructor(code: Int, message: String) {
        this.code = code
        this.message = message
    }

    constructor()

    companion object {

        const val CODE_OK = 0
        const val CODE_FAILED = 1//失败
        const val CODE_SERVER_ERR = 2//出错
        const val CODE_INVALID = 5//无效
        const val CODE_TOKEN_OUT_DATE = 6//token过期


        fun <T> error(err: String?): ResponseMessage<T> {
            return ResponseMessage(CODE_FAILED, err
                ?: "null")
        }
    }

}
