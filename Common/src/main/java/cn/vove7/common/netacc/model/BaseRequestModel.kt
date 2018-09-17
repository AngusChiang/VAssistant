package cn.vove7.common.netacc.model

import cn.vove7.common.netacc.tool.SignHelper.MD5
import java.io.Serializable
import javax.crypto.Cipher.SECRET_KEY

/**
 * # BaseRequestModel
 *
 * @author Administrator
 * 2018/9/16
 */
class BaseRequestModel<T : Serializable>(var body: T? = null, val arg1: String? = null) {
    val timestamp = ((System.currentTimeMillis() / 1000).toInt()).toString()
    var sign: String = MD5(timestamp + SECRET_KEY)
}