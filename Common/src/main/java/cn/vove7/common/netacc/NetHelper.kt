package cn.vove7.common.netacc

import android.os.Handler
import android.os.Looper
import cn.vove7.common.BuildConfig
import cn.vove7.common.accessibility.AccessibilityApi
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.appbus.AppBus
import cn.vove7.common.datamanager.history.CommandHistory
import cn.vove7.common.datamanager.parse.model.Action
import cn.vove7.common.datamanager.parse.model.ActionScope
import cn.vove7.common.interfaces.DownloadInfo
import cn.vove7.common.interfaces.DownloadProgressListener
import cn.vove7.common.model.UserInfo
import cn.vove7.common.netacc.model.BaseRequestModel
import cn.vove7.common.netacc.model.LastDateInfo
import cn.vove7.common.netacc.model.RequestParseModel
import cn.vove7.common.netacc.model.ResponseMessage
import cn.vove7.common.utils.GsonHelper
import cn.vove7.common.utils.ThreadPool
import cn.vove7.vtp.log.Vog
import com.google.gson.reflect.TypeToken
import com.liulishuo.okdownload.DownloadListener
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause

import okhttp3.*
import java.io.File
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
    /**
     * 网络post请求 内容格式为json
     * @param url String
     * @param model BaseRequestModel<*>?
     * @param requestCode Int
     * @param callback OnResponse<T>
     */
    inline fun <reified T> postJson(
            url: String, model: BaseRequestModel<*>? = BaseRequestModel<Any>(),
            requestCode: Int = 0, crossinline callback: OnResponse<T> = { _, _ -> }
    ) {
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
        call(call, requestCode, callback)
    }

    inline fun <reified T> call(call: Call, requestCode: Int = 0,
                                crossinline callback: OnResponse<T>) {
        prepareIfNeeded()
        val handler = Handler(Looper.getMainLooper())
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
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
                        val bean = GsonHelper.fromResponseJson<T>(s)
                        if (bean?.isInvalid() == true || bean?.tokenIsOutdate() == true) {//无效下线
                            if (UserInfo.isLogin()) {
                                GlobalApp.toastWarning("用户身份过期请重新登陆")
                            }
                            AppBus.post(AppBus.EVENT_FORCE_OFFLINE)
                        }
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

    fun prepareIfNeeded() {
        if (Looper.myLooper() == null)
            Looper.prepare()
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
                Vog.d(this, "taskEnd ---> $cause")
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
                Vog.d(this, "fetchStart ---> ${contentLength}")
            }

            override fun fetchProgress(task: DownloadTask, blockIndex: Int, increaseBytes: Long) {
                sum += increaseBytes

                Vog.d(this, "fetchProgress ---> ${sum}/$total")
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
        postJson<List<Action>>(ApiUrls.CLOUD_PARSE,
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
        NetHelper.postJson<LastDateInfo>(ApiUrls.GET_LAST_DATA_DATE) { _, b ->
            if (b?.isOk() == true && b.data != null) {
                back.invoke(b.data!!)
            } else {
                back.invoke(null)
            }
        }
    }

}
