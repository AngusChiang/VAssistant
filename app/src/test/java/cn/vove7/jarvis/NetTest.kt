package cn.vove7.jarvis

import cn.vove7.common.netacc.ApiUrls
import cn.vove7.common.netacc.WrapperNetHelper
import cn.vove7.common.netacc.model.LastDateInfo
import cn.vove7.vtp.net.NetHelper
import cn.vove7.vtp.net.WrappedRequestCallback
import org.junit.Test
import java.io.Serializable
import java.lang.Thread.sleep

/**
 * # NetTest
 *
 * @author 11324
 * 2019/3/18
 */
class NetTest {

    @Test
    fun main() {
        val call = NetHelper.get<String>("https://www.baidu.com/") {
            success { _, s ->
                println(s)
            }
            fail { _, e ->
                println(e.message)
            }
            before { println("before") }
            end { println("end") }
            cancel { println("取消") }
        }
        sleep(10)
//        call.cancel()
        sleep(10000)
    }

    @Test
    fun testNet() {
        val l = Object()
        WrapperNetHelper.postJson<LastDateInfo>(ApiUrls.GET_LAST_DATA_DATE) {
            success { _, b ->
                print(b)
            }
            fail { _, e ->
                e.printStackTrace()
            }
            end {
                l.notifyASync()
            }
        }
        l.waitASync()
    }

}

fun Object.notifyASync() {
    synchronized(this) {
        notify()
    }
}

fun Object.waitASync() {
    synchronized(this) {
        wait()
    }
}

/**
 * 封装示例：请求体封装
 * (RequestModel(sign, timestamp, [body]))
 *        |
 *        |
 *       http
 *        |
 *        ↓
 * ResponseMessage(code,message,[data])
 */
object WrapperNetHelper {

    inline fun <reified T> postJson(
            url: String, model: Any? = null, requestCode: Int = -1, arg1: String? = null,
            crossinline callback: WrappedRequestCallback<ResponseMessage<T>>.() -> Unit) {

        NetHelper.postJson(url, RequestModel(model, arg1), requestCode,callback = callback)

    }

    inline fun <reified T> get(
            url: String, params: Map<String, String>? = null, requestCode: Int = 0,
            callback: WrappedRequestCallback<ResponseMessage<T>>.() -> Unit
    ) {

        NetHelper.get(url, params, requestCode, callback)

    }
}

class RequestModel<T : Any>(var body: T? = null, val arg1: String? = null)
    : Serializable {
    val timestamp = (System.currentTimeMillis() / 1000)
    val userId = -1L
    var sign: String = "" // 签名数据 signData(GsonHelper.toJson(body), userId, timestamp)
    val userToken = null

}


data class M(
        var a: Int? = null,
        var b: String? = null,
        var c: Array<Int>? = null
)

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
