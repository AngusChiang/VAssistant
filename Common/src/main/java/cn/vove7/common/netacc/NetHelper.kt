package cn.vove7.common.netacc

import android.os.Handler
import android.os.Looper
import cn.vove7.common.BuildConfig
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vassistant.plugininterface.app.GlobalApp
import cn.vassistant.plugininterface.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.history.CommandHistory
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.model.UserInfo
import cn.vove7.common.model.VipPrice
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.model.LastDateInfo
import cn.vove7.common.netacc.model.RequestParseModel
import cn.vove7.common.netacc.model.ResponseMessage
import cn.vove7.common.utils.GsonHelper
import cn.vove7.common.utils.ThreadPool
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

    private var timeout = 15L

    fun <T> postJson(url: String, model: BaseRequestModel<*>? = BaseRequestModel<Any>(),
                     type: Type = StringType, requestCode: Int = 0, callback: OnResponse<T>? = null) {
        val client = OkHttpClient.Builder()
                .readTimeout(timeout, TimeUnit.SECONDS).build()

        val json = GsonHelper.toJson(model)
        Vog.d(this, "postJson ---> $url\n $json")
        val requestBody = FormBody.create(MediaType
                .parse("application/json; charset=utf-8"), json)

        val request = Request.Builder().url(url)
                .post(requestBody)
                .build()
        val call = client.newCall(request)
        call(call, type, requestCode, callback)
    }

    private fun <T> call(call: Call, type: Type, requestCode: Int = 0, callback: OnResponse<T>?) {
        prepareIfNeeded()
        val handler = Handler(Looper.getMainLooper())
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                GlobalLog.err("net failure: " + e.message)
                handler.post {
                    callback?.invoke(requestCode, ResponseMessage.error(e.message))
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {//响应成功更新UI
                if (response.isSuccessful) {
                    val s = response.body()?.string()
                    try {
                        Vog.d(this, "onResponse --->\n$s")
                        val bean = GsonHelper.fromJsonObj<T>(s, type)
                        if (bean?.isInvalid() == true || bean?.tokenIsOutdate() == true) {//无效下线
                            if (UserInfo.isLogin()) {
                                GlobalApp.toastShort("用户身份过期请重新登陆")
                            }
                            AppBus.post(AppBus.EVENT_FORCE_OFFLINE)
                        }
                        handler.post {
                            callback?.invoke(requestCode, bean)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        GlobalLog.err(e.message)
                        handler.post {
                            GlobalLog.err("json err data: ${e.message}\n $s ")
                            callback?.invoke(requestCode, ResponseMessage.error(e.message))
                        }
                    }
                } else handler.post {
                    GlobalLog.err("net: " + response.message())
                    callback?.invoke(requestCode, null)
                }
            }
        })
    }

    private fun prepareIfNeeded() {
        if (Looper.myLooper() == null)
            Looper.prepare()
    }

    val StringType: Type by lazy {
        object : TypeToken<ResponseMessage<String>>() {}.type
    }
    val IntType: Type by lazy {
        object : TypeToken<ResponseMessage<Int>>() {}.type
    }
    val LastDateInfoType: Type by lazy {
        object : TypeToken<ResponseMessage<LastDateInfo>>() {}.type
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
    val ActionListType: Type by lazy {
        object : TypeToken<ResponseMessage<List<Action>>>() {}.type
    }
    val VipPriceListType: Type by lazy {
        object : TypeToken<ResponseMessage<List<VipPrice>>>() {}.type
    }
    val ActionNodeListType: Type by lazy {
        object : TypeToken<ResponseMessage<List<ActionNode>>>() {}.type
    }
    val MapType: Type by lazy {
        object : TypeToken<Map<String, Any>>() {}.type
    }

    inline fun <reified T> getType(): Type {
        return object : TypeToken<T>() {}.type
    }

    /**
     * 命令记录，
     * 打开记录，
     *
     * @param his CommandHistory
     */
    fun uploadUserCommandHistory(his: CommandHistory) {
        ThreadPool.runOnPool {
            if (BuildConfig.DEBUG /*|| !AppConfig.userExpPlan*/) return@runOnPool
            prepareIfNeeded()
            postJson<Any>(ApiUrls.UPLOAD_CMD_HIS, BaseRequestModel(his)) { _, b ->
                if (b?.isOk() == true) {
                    Vog.d(this, "uploadUserCommandHistory ---> usccc")
                } else {
                    Vog.d(this, "uploadUserCommandHistory ---> ${b?.message}")
                }
            }
        }
    }


    /**
     * 云解析
     * @param cmd String
     * @param scope ActionScope?
     * @param onResult (List<Action>?) -> Unit
     */
    fun cloudParse(cmd: String, scope: ActionScope? = AccessibilityApi
            .accessibilityService?.currentScope, onResult: (List<Action>?) -> Unit) {
//        thread {
        postJson<List<Action>>(ApiUrls.CLOUD_PARSE, type = ActionListType,
                model = BaseRequestModel(RequestParseModel(cmd, scope))) { _, b ->
            if (b?.isOk() == true) {
                Vog.d(this, "cloudParse ---> ${b.data}")
                onResult.invoke(b.data)
            } else {
                onResult.invoke(null)
                GlobalLog.err(b?.message)
            }
        }
//        }
    }

    fun getLastInfo(back: (LastDateInfo?) -> Unit) {
        NetHelper.postJson<LastDateInfo>(ApiUrls.GET_LAST_DATA_DATE, type = NetHelper.LastDateInfoType) { _, b ->
            if (b?.isOk() == true && b.data != null) {
                back.invoke(b.data!!)
            } else {
                back.invoke(null)
            }
        }
    }

}