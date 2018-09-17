package cn.vove7.common.netacc.model

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
        return code == CODE_OK
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

        val CODE_OK = 0
        val CODE_FAILED = 1//失败
        val CODE_SERVER_ERR = 2//出错

        fun <T> error(err: String?): ResponseMessage<T> {
            return ResponseMessage(CODE_FAILED, err
                ?: "null")
        }
    }

}
