package cn.vove7.common.netacc

import android.os.Handler
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.model.ResponseMessage
import cn.vove7.common.netacc.tool.OneGson
import cn.vove7.common.netacc.tool.SignHelper
import cn.vove7.vtp.log.Vog
import com.google.gson.Gson
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
                 type: Type, requestCode: Int = 0, callback: OnResponse<T>) {
        val client = OkHttpClient.Builder()
                .readTimeout(timeout, TimeUnit.SECONDS).build()
        SignHelper.signParam(params)
        val bodyBuilder = FormBody.Builder()
        params?.forEach { it ->
            bodyBuilder.add(it.key, it.value)
        }
        val request = Request.Builder().url(url)
                .post(bodyBuilder.build()).build()
        val call = client.newCall(request)
        call(call, type, requestCode, callback)
    }

    fun <T> postJson(url: String, model: BaseRequestModel<*>,
                     type: Type, requestCode: Int = 0, callback: OnResponse<T>) {
        val client = OkHttpClient.Builder()
                .readTimeout(timeout, TimeUnit.SECONDS).build()

        val json = Gson().toJson(model)
        Vog.d(this, "postJson ---> $json")
        val requestBody = FormBody.create(MediaType
                .parse("application/json; charset=utf-8"), json)

        val request = Request.Builder().url(url)
                .post(requestBody)
                .build()
        val call = client.newCall(request)
        call(call, type, requestCode, callback)
    }

    private fun<T> call(call: Call, type: Type, requestCode: Int = 0, callback: OnResponse<T>) {
        val handler = Handler()
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {//响应失败更新UI
                GlobalLog.err("net failure: " + e.message)
                handler.post {
                    callback.invoke(requestCode, ResponseMessage.error(e.message))
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {//响应成功更新UI
                if (response.isSuccessful) {
                    val s = response.body()?.string()
                    try {
//                        Vog.d(this, "onResponse $url --->\n$s")
                        val bean = OneGson.fromJsonObj<T>(s, type)
                        handler.post {
                            callback.invoke(requestCode, bean)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        GlobalLog.err(e.message)
                        handler.post {
                            GlobalLog.err("json err data: ${e.message}\n $s ")
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