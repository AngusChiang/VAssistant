package cn.vove7.common.net

import cn.vove7.common.BuildConfig
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.datamanager.history.CommandHistory
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.interfaces.DownloadInfo
import cn.vove7.common.interfaces.DownloadProgressListener
import cn.vove7.common.model.UserInfo
import cn.vove7.common.net.model.BaseRequestModel
import cn.vove7.common.net.model.LastDateInfo
import cn.vove7.common.net.model.RequestParseModel
import cn.vove7.common.net.model.ResponseMessage
import cn.vove7.common.net.tool.SecureHelper
import cn.vove7.common.utils.LooperHelper
import cn.vove7.common.utils.ThreadPool
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.net.GsonHelper
import cn.vove7.vtp.net.NetHelper
import cn.vove7.vtp.net.WrappedRequestCallback
import com.google.gson.reflect.TypeToken
import com.liulishuo.okdownload.DownloadListener
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import okhttp3.*
import java.io.File
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * # NetHelper
 *
 * @author Vove7
 * 2018/9/12
 */

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

    inline fun <reified T> post(
            url: String, model: Any? = null, requestCode: Int = -1, arg1: String? = null,
            crossinline onsuccess: (ResponseMessage<T>) -> Unit
    ) {
        postJson<T>(url, model, requestCode, arg1) {
            success { _, responseMessage ->
                onsuccess(responseMessage)
            }
        }
    }

    inline fun <reified T> postJson(
            url: String, model: Any? = null, requestCode: Int = -1, arg1: String? = null,
            crossinline callback: WrappedRequestCallback<ResponseMessage<T>>.() -> Unit
    ) {
        val reqModel = BaseRequestModel(model, arg1)
        val ts = (System.currentTimeMillis() / 1000)
        val reqJson = GsonHelper.toJson(reqModel)
        val sign = SecureHelper.signData(reqJson, ts)
        val headers = mapOf(
                "versionCode" to "${BuildConfig.VERSION_CODE}",
                "timestamp" to ts.toString(),
                "token" to (UserInfo.getUserToken() ?: ""),
                "sign" to sign
        )
        NetHelper.postJsonString(url, reqJson, requestCode, headers = headers, callback = callback)
    }


    inline fun <reified T> getType(): Type {
        return object : TypeToken<T>() {}.type
    }

    /**
     * @param url          下载连接
     * @param destFileDir  下载的文件储存目录
     * @param destFileName 下载文件名称
     * @param listener     下载监听
     * @return id
     */
    fun <T> download(url: String, destFileDir: String, destFileName: String, data: T? = null,
                     listener: DownloadProgressListener): DownloadTask {
        val taskId = url.hashCode()
        val di = DownloadInfo(taskId,
                "$destFileDir/$destFileName", data)

        val task = DownloadTask.Builder(url, File(destFileDir))
                .setFilename(destFileName)
                // the minimal interval millisecond for callback progress
                .setMinIntervalMillisCallbackProcess(500)
                // do re-download even if the task has already been completed in the past.
                .setPassIfAlreadyCompleted(true)
                .setConnectionCount(1)
                .setFilenameFromResponse(true)
                .build()

        var total = 100L
        var sum = 0L
        listener.onStart(di)
        task.enqueue(object : DownloadListener {
            override fun connectTrialEnd(task: DownloadTask, responseCode: Int, responseHeaderFields: MutableMap<String, MutableList<String>>) {
            }

            override fun fetchEnd(task: DownloadTask, blockIndex: Int, contentLength: Long) {
            }

            override fun downloadFromBeginning(task: DownloadTask, info: BreakpointInfo, cause: ResumeFailedCause) {
            }

            override fun taskStart(task: DownloadTask) {
            }

            override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: java.lang.Exception?) {
                Vog.d("taskEnd ---> $cause")
                when (cause) {
                    EndCause.CANCELED -> listener.onCancel(di)
                    EndCause.COMPLETED -> listener.onSuccess(di, File(destFileDir, destFileName))
                    EndCause.ERROR -> listener.onFailed(di, realCause
                        ?: java.lang.Exception("unknown"))
                    EndCause.FILE_BUSY -> listener.onFailed(di, Exception("文件忙"))
                    EndCause.SAME_TASK_BUSY -> listener.onFailed(di, Exception("任务忙"))
                    EndCause.PRE_ALLOCATE_FAILED -> listener.onFailed(di, Exception("申请失败"))
                }

                realCause?.printStackTrace()
            }

            override fun connectTrialStart(task: DownloadTask, requestHeaderFields: MutableMap<String, MutableList<String>>) {
            }

            override fun downloadFromBreakpoint(task: DownloadTask, info: BreakpointInfo) {
            }

            override fun fetchStart(task: DownloadTask, blockIndex: Int, contentLength: Long) {
                total += contentLength
                Vog.d(contentLength)
            }

            override fun fetchProgress(task: DownloadTask, blockIndex: Int, increaseBytes: Long) {
                sum += increaseBytes

                Vog.d("$sum/$total")
                listener.onDownloading(di, (sum * 1.0f / total * 100).toInt())
            }

            override fun connectEnd(task: DownloadTask, blockIndex: Int, responseCode: Int, responseHeaderFields: MutableMap<String, MutableList<String>>) {
            }

            override fun connectStart(task: DownloadTask, blockIndex: Int, requestHeaderFields: MutableMap<String, MutableList<String>>) {
            }
        })
        // 代码中使用
        return task
    }

    /**
     * 命令记录，
     * 打开记录，
     *
     * @param his CommandHistory
     */
    fun uploadUserCommandHistory(his: CommandHistory) {
        if (BuildConfig.DEBUG || !AppConfig.userExpPlan) return
        ThreadPool.runOnPool {
            LooperHelper.prepareIfNeeded()
            post<Any>(ApiUrls.UPLOAD_CMD_HIS, his) { b ->
                if (!b.isOk()) {
                    Vog.d(b.message)
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
        postJson<List<Action>>(ApiUrls.CLOUD_PARSE,
                model = RequestParseModel(cmd, scope)) {
            success { _, b ->
                if (b.isOk()) {
                    Vog.d("cloudParse ---> ${b.data}")
                    onResult.invoke(b.data)
                } else {
                    onResult.invoke(null)
                    GlobalLog.err(b.message)
                }
            }
            fail { _, exception ->
                onResult.invoke(null)
                GlobalLog.err(exception)
            }
        }
//        }
    }

    fun getLastInfo(back: (LastDateInfo?) -> Unit) {
        postJson<LastDateInfo>(ApiUrls.GET_LAST_DATA_DATE) {
            success { _, b ->
                if (b.isOk()) {
                    back.invoke(b.data)
                } else {
                    back.invoke(null)
                }
            }
            fail { _, e ->
                GlobalLog.err(e)
                back.invoke(null)
            }
        }
    }

}

/**
 * 网络post请求 内容格式为json
 * @param url String
 * @param model Any? 请求体
 * @param requestCode Int
 * @param callback WrappedRequestCallback<T>.()
 */
inline fun <reified T> NetHelper.postJsonString(
        url: String, json: String? = null, requestCode: Int = 0,
        headers: Map<String, String>? = null,
        callback: WrappedRequestCallback<T>.() -> Unit
): Call {
    val client = OkHttpClient.Builder()
            .readTimeout(timeout, TimeUnit.SECONDS).build()

    val requestBody = FormBody.create(MediaType
            .parse("application/json; charset=utf-8"), json)

    Vog.d("post ($url)\n$json")
    val request = Request.Builder().url(url)
            .post(requestBody)
            .apply {
                headers?.forEach {
                    addHeader(it.key, it.value)
                }
            }
            .build()
    val call = client.newCall(request)
    call(url, call, requestCode, callback)
    return call
}
