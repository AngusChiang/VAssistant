package cn.vove7.common.bridges

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.vtp.log.Vog
import com.stericson.RootShell.RootShell
import java.io.DataInputStream
import java.io.DataOutputStream

/**
 *
 */
object RootHelper {

    /**
     * 判断即是否授予root权限
     */
    @JvmStatic
    fun isRoot(): Boolean = hasRoot(100)

    @JvmStatic
    fun hasRoot(timeOut: Int = 5000): Boolean = RootShell.isAccessGiven(timeOut, 0)


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
                DataInputStream(p.inputStream).use { dis ->
                    dos.flush()
                    dos.writeBytes("exit\n")
                    dos.flush()
                    while ((dis.readLine().also { result.appendln(it) }) != null);
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
    fun execWithSu(cmd: String): String? {
        GlobalLog.log("execWithSu ---> $cmd")
        val result = StringBuilder()
        val p = Runtime.getRuntime().exec("su")// 经过Root处理的android系统即有su命令
        DataOutputStream(p.outputStream).use { dos ->
            DataInputStream(p.inputStream).use { dis ->
                dos.writeBytes(cmd + "\n")
                dos.flush()
                dos.writeBytes("exit\n")
                dos.flush()
                while ((dis.readLine().also { result.appendln(it) }) != null);
                p.waitFor()
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
        if (!hasRoot()) return false
        Vog.d("openAppAccessService ---> $serviceName")
        //同时不关闭其他
        return try {
            execWithSu(buildList("$pkg/$serviceName"))
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
            appendln()
            appendln("settings put secure accessibility_enabled 1")
        }.also {
            Vog.d("buildList ---> $it")
        }
    }

}
