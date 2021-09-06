package cn.vove7.common.bridges

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.Context
import android.view.accessibility.AccessibilityManager
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.model.ResultBox
import cn.vove7.jadb.AdbClient
import cn.vove7.jadb.AdbCrypto
import cn.vove7.vtp.log.Vog
import com.stericson.RootShell.RootShell
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.Inet4Address

/**
 *
 */
@Suppress("unused")
object ShellHelper {

    @SuppressLint("StaticFieldLeak")
    private var _adbClient: AdbClient? = null

    private var rooted = false

    /**
     * 判断即是否授予root权限
     */
    @JvmStatic
    fun isRoot(): Boolean = hasRoot(100)

    @JvmStatic
    fun hasRoot(timeOut: Int = 5000): Boolean = run {
        if (!rooted) rooted = RootShell.isAccessGiven(timeOut, 0)
        rooted
    }

    fun hasRootOrAdb() = hasRoot(500) || SystemBridge.isWirelessAdbEnabled()

    /**
     * 执行无root命令
     * @param cmd String 命令
     * @return String 结果
     */
    fun exec(cmd: String): String {
        val result = StringBuilder()
        Vog.d("execRootCmd ---> $cmd")
        try {
            val p = Runtime.getRuntime().exec(cmd)
            DataOutputStream(p.outputStream).use { dos ->
                BufferedReader(InputStreamReader(p.inputStream)).use { dis ->
                    dos.flush()
                    dos.writeBytes("exit\n")
                    dos.flush()
                    while ((dis.readLine().also { result.appendLine(it) }) != null);
                    p.waitFor()
                }
            }
        } catch (e: Exception) {
            GlobalLog.err(e)
        }
        return result.toString().also {
            GlobalLog.log("exec result -> $it")
        }
    }

    /**
     * 执行root命令
     * @return String 结果
     */
    @Throws
    @JvmOverloads
    fun execWithSu(cmd: String, waitResult: Boolean = true): String {
        GlobalLog.log("execWithSu ---> $cmd")
        val result = StringBuilder()
        val p = Runtime.getRuntime().exec("su")// 经过Root处理的android系统即有su命令
        DataOutputStream(p.outputStream).use { dos ->
            BufferedReader(InputStreamReader(p.inputStream)).use { dis ->
                dos.writeBytes(cmd + "\n")
                dos.flush()
                dos.writeBytes("exit\n")
                dos.flush()
                if (waitResult) {
                    while ((dis.readLine().also { result.appendLine(it) }) != null);
                    p.waitFor()
                }
            }
        }
        return result.toString().also {
            GlobalLog.log("exec result -> $it")
        }
    }

    /**
     * 执行命令但不关注结果输出
     */
    fun execSuSilent(cmd: String): Int {
        var result = -1
        try {
            val p = Runtime.getRuntime().exec("su")
            DataOutputStream(p.outputStream).use { dos ->
                dos.writeBytes(cmd + "\n")
                dos.flush()
                dos.writeBytes("exit\n")
                dos.flush()
                p.waitFor()
                result = p.exitValue()
            }
        } catch (e: Exception) {
            GlobalLog.err(e)
            e.printStackTrace()
        }
        return result
    }

    fun openAppAccessService(pkg: String, serviceName: String): Boolean {
        Vog.d("openAppAccessService ---> $serviceName")
        //同时不关闭其他
        return try {
            execAuto(buildList("$pkg/$serviceName"))
            true
        } catch (e: Exception) {
            GlobalLog.err(e.message)
            GlobalApp.toastError("无障碍自动开启失败")
            false
        } finally {
            Vog.d("openAppAccessService ---> 申请结束")
        }
    }

    private fun buildList(s: String): String {
        val p = "settings put secure enabled_accessibility_services "
        return buildString {
            append(p)
            append(s)
            val am = GlobalApp.APP.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
                ?.forEach {
                    append(":${it.id}")
                }
            appendLine()
            appendLine("settings put secure accessibility_enabled 1")
        }.also {
            Vog.d("buildList ---> $it")
        }
    }

    fun adbEnable() = SystemBridge.isWirelessAdbEnabled()

    @JvmOverloads
    fun execAuto(cmd: String, waitResult: Boolean = true) = when {
        hasRoot() -> execWithSu(cmd, waitResult)
        SystemBridge.isWirelessAdbEnabled() -> execWithAdb(cmd, waitResult)
        else -> throw RuntimeException("no root or adb permission")
    }

    @JvmOverloads
    fun execWithAdb(cmd: String?, waitResult: Boolean = true, close: Boolean = waitResult, timeout: Int = 6000): String? {
        val jadb = _adbClient ?: AdbClient(
            GlobalApp.APP,
            Inet4Address.getLoopbackAddress().hostName,
            SystemBridge.adbPort(),
            adbCrypto = AdbCrypto.get(GlobalApp.APP),
            name = "VAssistant"
        ).also {
            _adbClient = it
            it.connect()
        }

        val stream = jadb.shellCommand(cmd ?: " ")
        if (!waitResult) {
            if (close) {
                stream.close()
            }
            return null
        }
        val box = ResultBox<String>()
        stream.onClose {
            box.setAndNotify(String(data))
        }
        return box.blockedGet(false, timeout.toLong()).also {
            stream.onClose(null)
        }
    }

    fun release() {
        _adbClient?.close()
        _adbClient = null
    }

}
