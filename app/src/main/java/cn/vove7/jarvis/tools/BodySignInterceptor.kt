package cn.vove7.jarvis.tools

import cn.daqinjia.android.common.logi
import cn.vove7.common.app.AppConfig
import cn.vove7.common.model.UserInfo
import cn.vove7.common.net.tool.SecureHelper
import cn.vove7.quantumclock.QuantumClock
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset

/**
 * # BodySignInterceptor
 *
 * @author Vove
 * 2020/9/1
 */
class BodySignInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var req = chain.request()
        if (req.method == "POST") {
            val data = req.body?.let {
                val buffer = Buffer()
                it.writeTo(buffer)
                val ct = req.body?.contentType()

                val cs = ct?.charset(Charset.forName("UTF-8")) ?: Charset.forName("UTF-8")
                buffer.readString(cs)
            } ?: ""
            val arg1 = req.header("arg1")

            val json = """
                {
                ${if (data.isEmpty()) "" else """"body": $data ,"""}
                "userId": ${UserInfo.getUserId()}
                ${if (arg1 == null) "" else """, "arg1":"$arg1" """}
                }
            """.trimIndent()

            val ts = (QuantumClock.currentTimeMillis / 1000)
            val sign = SecureHelper.signData(json, ts)

            "json: $json\nsign: $sign".logi()

            val headers = mapOf(
                    "versionCode" to "${AppConfig.versionCode}",
                    "timestamp" to ts.toString(),
                    "token" to (UserInfo.getUserToken() ?: ""),
                    "sign" to sign
            )

            req = req.newBuilder().apply {
                headers.forEach { (t, u) ->
                    this.addHeader(t, u)
                }
            }.post(json.toRequestBody("application/json; charset=utf-8".toMediaType())).build()

        }
        return chain.proceed(req)
    }
}