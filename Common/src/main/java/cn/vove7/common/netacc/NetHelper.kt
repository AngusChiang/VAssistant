package cn.vove7.common.netacc

import android.os.Handler
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.model.UserInfo
import cn.vove7.common.model.VipPrice
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.model.ResponseMessage
import cn.vove7.common.netacc.tool.OneGson
import cn.vove7.common.netacc.tool.SecureHelper
import cn.vove7.common.utils.GsonHelper
import cn.vove7.vtp.log.Vog
import com.google.gson.reflect.TypeToken
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
    fun <T> get(url: String, params: Map<String, String>? = null,
                type: Type, requestCode: Int = 0, callback: OnResponse<T>) {
        val client = OkHttpClient.Builder()
                .readTimeout(timeout, TimeUnit.SECONDS).build()
        SecureHelper.signParam(params)

        val requestBuilder = Request.Builder().url(url).get()
        params?.forEach { it ->
            requestBuilder.addHeader(it.key, it.value)
        }
        val req = requestBuilder.build()
        val call = client.newCall(req)
        call(call, type, requestCode, callback)
    }

    fun <T> post(url: String, params: Map<String, String>? = null,
                 type: Type, requestCode: Int = 0, callback: OnResponse<T>) {
        val client = OkHttpClient.Builder()
                .readTimeout(timeout, TimeUnit.SECONDS).build()
        SecureHelper.signParam(params)
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
                     type: Type = StringType, requestCode: Int = 0, callback: OnResponse<T>) {
        val client = OkHttpClient.Builder()
                .readTimeout(timeout, TimeUnit.SECONDS).build()

        val json = GsonHelper.toJson(model)
        Vog.d(this, "postJson ---> $json")
        val requestBody = FormBody.create(MediaType
                .parse("application/json; charset=utf-8"), json)

        val request = Request.Builder().url(url)
                .post(requestBody)
                .build()
        val call = client.newCall(request)
        call(call, type, requestCode, callback)
    }

    private fun <T> call(call: Call, type: Type, requestCode: Int = 0, callback: OnResponse<T>) {
        val handler = Handler()
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {//响应失败更新UI
                e.printStackTrace()
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
                        Vog.d(this, "onResponse --->\n$s")
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

    val StringType: Type by lazy {
        object : TypeToken<ResponseMessage<String>>() {}.type
    }
    val IntType: Type by lazy {
        object : TypeToken<ResponseMessage<Int>>() {}.type
    }
    val UserInfoType: Type by lazy {
        object : TypeToken<ResponseMessage<UserInfo>>() {}.type
    }
    val MarkedDataListType: Type by lazy {
        object : TypeToken<ResponseMessage<List<MarkedData>>>() {}.type
    }

    val DoubleListType: Type by lazy {
        object : TypeToken<ResponseMessage<List<Double>>>() {}.type
    }
    val VipPriceListType: Type by lazy {
        object : TypeToken<ResponseMessage<List<VipPrice>>>() {}.type
    }
    val ActionNodeListType: Type by lazy {
        object : TypeToken<ResponseMessage<List<ActionNode>>>() {}.type
    }

}