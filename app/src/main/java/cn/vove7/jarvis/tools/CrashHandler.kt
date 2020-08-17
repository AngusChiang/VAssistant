package cn.vove7.jarvis.tools

import android.content.Context
import android.os.Build
import cn.vove7.common.app.AppConfig
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.model.UserInfo
import cn.vove7.common.net.ApiUrls
import cn.vove7.common.net.WrapperNetHelper
import cn.vove7.common.utils.StorageHelper
import cn.vove7.common.utils.TextHelper
import cn.vove7.common.utils.TextPrinter
import cn.vove7.common.utils.formatNow
import cn.vove7.jarvis.BuildConfig
import cn.vove7.quantumclock.QuantumClock
import cn.vove7.vtp.system.DeviceInfo
import cn.vove7.vtp.system.SystemHelper
import com.wanjian.cockroach.Cockroach
import java.io.File

/**
 * # CrashHandler
 *
 * @author Administrator
 * 9/25/2018
 */
object CrashHandler : Cockroach.ExceptionHandler {

    fun install() {
        Cockroach.install(this)
    }

    val context: Context by lazy {
        GlobalApp.APP
    }

    override fun handlerException(thread: Thread?, throwable: Throwable?) {
        throwable ?: return
        GlobalApp.toastError("发生异常，可将[帮助/日志]发送进行反馈")
        GlobalLog.err("发生异常 at $thread")
        GlobalLog.err(throwable)
        handler(throwable)
    }

    private fun handler(e: Throwable) {
        val headerInfo = SystemHelper.getDeviceInfo(context).string()
        val errFile = File(StorageHelper.extPath, "crash.log")
        try {
            val info = TextPrinter().apply {
                println(headerInfo)
                e.printStackTrace(this)
            }.toString()

            try {//输出和sd卡
                errFile.writeText(info)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (!BuildConfig.DEBUG)
                WrapperNetHelper.postJson<Any>(ApiUrls.CRASH_HANDLER, info) {}
        } catch (e1: Exception) {//文件读写
            if (!BuildConfig.DEBUG)
                WrapperNetHelper.postJson<Any>(ApiUrls.CRASH_HANDLER,
                        headerInfo + e.message + "crash上传失败${e1.message}") {}
        }
    }
}

fun DeviceInfo.string(): String {
    return buildString {
        append("appId: ").appendln(BuildConfig.APPLICATION_ID)
        append("userId: ").appendln(UserInfo.getUserId())
        append("appVersion: ").appendln(AppConfig.versionName)
        append("versionCode: ").appendln(AppConfig.versionCode)
        append("时间: ").appendln(formatNow())
        append("email: ").appendln(UserInfo.getEmail())
        append("manufacturerName: ").appendln(manufacturerName)
        append("productName: ").appendln(productName)
        append("brandName: ").appendln(brandName)
        append("model: ").appendln(model)
        append("boardName: ").appendln(boardName)
        append("deviceName: ").appendln(deviceName)
        append("serial: ").appendln(serial)
        append("sdkInt: ").appendln(sdkInt)
        append("androidVersion: ").appendln(androidVersion)
        append("ABI  : ").appendln(TextHelper.arr2String(Build.SUPPORTED_ABIS))
        append("运行时间：" + (QuantumClock.currentTimeMillis - GlobalApp.launchTime) / 1000 + "s")
    }
}
