package cn.vove7.common.netacc

import android.os.Handler
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.model.ResponseMessage
import cn.vove7.common.netacc.tool.OneGson
import cn.vove7.vtp.log.Vog
import okhttp3.*
import java.io.IOException
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * # NetHelper
 *
 * @author 17719247306
 * 2018/9/12
 */
typealias OnResponse<T> = (Int, ResponseMessage<T>?) -> Unit

object NetHelper {

    var timeout = 15L
    fun <T> post(url: String, params: Map<String, String>? = null,
                 type: Type, callback: OnResponse<T>, requestCode: Int = 0) {
        val client = OkHttpClient.Builder()
                .readTimeout(timeout, TimeUnit.SECONDS).build()

        val bodyBuilder = FormBody.Builder()
        bodyBuilder.add("timestamp", (System.currentTimeMillis() / 1000).toInt().toString())
        params?.forEach { it ->
            bodyBuilder.add(it.key, it.value)
        }
        val handler = Handler()

        val request = Request.Builder().url(url)
                .post(bodyBuilder.build()).build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {//响应失败更新UI
                GlobalLog.err("net: " + e.message)
                handler.post {
                    callback.invoke(requestCode, ResponseMessage.error(e.message))
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {//响应成功更新UI
                if (response.isSuccessful) {
                    val s = response.body()?.string()
                    try {
                        Vog.d(this,"onResponse $url --->\n$s")
                        val bean = OneGson.fromJsonObj<T>(s, type)
                        handler.post {
                            callback.invoke(requestCode, bean)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        GlobalLog.err(e.message)
                        handler.post {
                            GlobalLog.err("json data: ${e.message}\n $s ")
                            callback.invoke(requestCode, ResponseMessage.error(e.message))
                        }
                    }
                } else handler.post {
                    GlobalLog.err("net: " + response.message())
                    callback.invoke(requestCode, null)
                }

            }
        })
    }

}