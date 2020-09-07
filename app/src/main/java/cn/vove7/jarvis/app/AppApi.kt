package cn.vove7.jarvis.app

import cn.vove7.common.datamanager.AppAdInfo
import cn.vove7.common.datamanager.executor.entity.MarkedData
import cn.vove7.common.datamanager.history.CommandHistory
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.statusmap.ActionNode
import cn.vove7.common.model.UserInfo
import cn.vove7.common.model.VipPrice
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.net.model.LastDateInfo
import cn.vove7.common.net.model.RequestParseModel
import cn.vove7.common.net.model.ResponseMessage
import cn.vove7.jarvis.BuildConfig
import cn.vove7.jarvis.tools.BodySignInterceptor
import cn.vove7.vtp.log.Vog
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.net.Proxy

/**
 * # AppApi
 *
 * @author Vove
 * 2020/9/1
 */
@JvmSuppressWildcards
interface AppApi {
    companion object : AppApi by INS

    @POST(ApiUrls.REGISTER_BY_EMAIL)
    suspend fun registerByEmail(@Body user: UserInfo): ResponseMessage<String>

    @POST(ApiUrls.LOGIN)
    suspend fun login(@Body user: UserInfo): ResponseMessage<UserInfo>

    @POST(ApiUrls.GET_PRICES)
    suspend fun getVipPrices(): ResponseMessage<List<VipPrice>>

    @POST(ApiUrls.ACTIVATE_VIP)
    suspend fun activateVip(
            @Header("arg1") code: String
    ): ResponseMessage<String>


    @POST(ApiUrls.GET_USER_INFO)
    suspend fun getUserInfo(): ResponseMessage<UserInfo>

    @POST(ApiUrls.MODIFY_MAIL)
    suspend fun modifyMail(@Body user: UserInfo): ResponseMessage<Any>

    @POST(ApiUrls.MODIFY_NAME)
    suspend fun modifyName(@Body name: String): ResponseMessage<String>

    @POST(ApiUrls.MODIFY_PASS)
    suspend fun modifyPass(
            @Body old: String,
            @Header("arg1") newP: String
    ): ResponseMessage<Any>

    @POST(ApiUrls.SEND_RET_PASS_EMAIL_VER_CODE)
    suspend fun sendResetPassCode(
            @Body userEmail: String,
            @Header("arg1") type: String
    ): ResponseMessage<String>

    @POST(ApiUrls.RET_PASS_BY_EMAIL)
    suspend fun resetPassByEmailCode(
            @Body user: UserInfo,
            @Header("arg1") code: String
    ): ResponseMessage<String>

    @POST(ApiUrls.VERIFY_TOKEN)
    suspend fun verifyToken(): ResponseMessage<Any>

    @POST(ApiUrls.GET_LAST_DATA_DATE)
    suspend fun getLastDataInfo(): ResponseMessage<LastDateInfo>

    @POST(ApiUrls.SHARE_APP_AD_INFO)
    suspend fun shareAppAdInfo(
            @Body adInfo: AppAdInfo
    ): ResponseMessage<String>

    @POST(ApiUrls.DELETE_SHARE_APP_AD)
    suspend fun deleteAdAppInfo(@Body tag: String): ResponseMessage<Any>

    @POST(ApiUrls.DELETE_SHARE_MARKED)
    suspend fun deleteShareMarked(@Body tagId: String): ResponseMessage<Any>

    @POST(ApiUrls.SHARE_MARKED)
    suspend fun shareMarked(@Body data: MarkedData): ResponseMessage<String>

    @POST(ApiUrls.CRASH_HANDLER)
    suspend fun postCrashInfo(@Body info: String): ResponseMessage<Any>

    @POST(ApiUrls.SYNC_GLOBAL_INST)
    suspend fun syncGlobalInst(): ResponseMessage<List<ActionNode>>

    @POST(ApiUrls.SYNC_IN_APP_INST)
    suspend fun syncInAppInst(): ResponseMessage<List<ActionNode>>

    @POST(ApiUrls.SYNC_MARKED)
    suspend fun syncMarkedData(@Body types: String): ResponseMessage<List<MarkedData>>

    @POST(ApiUrls.SYNC_APP_AD)
    suspend fun syncMarkedAd(@Body pkgs: List<String>): ResponseMessage<List<AppAdInfo>>

    @POST(ApiUrls.DELETE_SHARE_INST)
    suspend fun deleteSharedInst(@Body tagId: String): ResponseMessage<Any>

    @POST(ApiUrls.UPGRADE_INST)
    suspend fun upgradeInst(@Body node: ActionNode): ResponseMessage<Int>

    @POST(ApiUrls.SHARE_INST)
    suspend fun shareInst(@Body node: ActionNode): ResponseMessage<String>

    @POST(ApiUrls.UPLOAD_CMD_HIS)
    suspend fun uploadCmdHistory(@Body his: CommandHistory): ResponseMessage<Any>

    @POST(ApiUrls.CLOUD_PARSE)
    suspend fun couldParse(@Body data: RequestParseModel): ResponseMessage<List<Action>>

}

private val INS by lazy {
    Retrofit.Builder().baseUrl(ApiUrls.SERVER_IP).apply {
        addConverterFactory(GsonConverterFactory.create(GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()))
        client(newHttpClient())
    }.build().create(AppApi::class.java)
}

fun newHttpClient(): OkHttpClient = OkHttpClient.Builder().apply {
    if (!BuildConfig.DEBUG) {
        proxy(Proxy.NO_PROXY)
    }
    addInterceptor(BodySignInterceptor())

    if (BuildConfig.DEBUG) {
        addInterceptor(
                HttpLoggingInterceptor { Vog.d(it) }.setLevel(
                        HttpLoggingInterceptor.Level.BODY
                )
        )
    }
}.build()
