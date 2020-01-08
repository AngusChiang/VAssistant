package cn.vove7.common.net.tool

import android.util.Base64
import cn.vove7.common.app.GlobalLog
import cn.vove7.vtp.log.Vog
import java.security.MessageDigest

/**
 * Created by IntelliJ IDEA.
 * User: Vove
 * Date: 2018/7/11
 * Time: 14:56
 */
object SecureHelper {
    private val SECRET_KEY = "vove777"

    private val HEX_DIGITS = "0123456789ABCDEF".toCharArray()

    fun signData(body: String?, time: Long?, key: String = SECRET_KEY): String {
        val content = (body ?: "") + time
        val md5 = MD5(content + key)
        Vog.d("加密：$content\n$md5")
        return md5
    }

    fun base64Encoder(content: String): String = Base64.encodeToString(content.toByteArray(), Base64.NO_WRAP)

    fun base64Decoder(content: String): String = String(Base64.decode(content.toByteArray(), Base64.NO_WRAP))

    fun MD5(s: String): String {
        try {
            val md = MessageDigest.getInstance("MD5")
            val bytes = md.digest(s.toByteArray(Charsets.UTF_8))
            return toHex(bytes)
        } catch (e: Exception) {
            GlobalLog.err(e)
            throw RuntimeException(e)
        }

    }

    fun MD5(vararg ss: String): String {
        val bu = StringBuilder()
        for (s in ss) {
            bu.append(s)
        }
        return MD5(bu.toString())
    }

    private fun toHex(bytes: ByteArray): String {
        val ret = StringBuilder(bytes.size * 2)
        for (aByte in bytes) {
            ret.append(HEX_DIGITS[(aByte.toInt() shr 4) and 0x0f])
            ret.append(HEX_DIGITS[aByte.toInt() and 0x0f])
        }
        return ret.toString()
    }

}


val String.md5: String get() = SecureHelper.MD5(this)
val String.base64: String get() = SecureHelper.base64Encoder(this)
