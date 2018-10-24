package cn.vove7.common.bridges

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.vtp.log.Vog
import java.io.DataInputStream
import java.io.DataOutputStream

/**
 *
 */
object RootHelper {
    private var mHaveRoot = false

    /**
     * 判断机器Android是否已经root，即是否获取root权限
     */
    fun isRoot(): Boolean {
        if (!mHaveRoot) {
            mHaveRoot = (execSuSilent("echo su") != -1).also {
                Vog.d(this, "isRoot ---> ${if (it) "have" else "didn't"} root")
            } // 通过执行测试命令来检测
        } else {
            Vog.d(this, "isRoot ---> have root")
        }
        return mHaveRoot
    }

    /**
     * 申请root权限
     */
    fun requestRoot(): Boolean {
        return isRoot()
    }

    fun exec(cmd: String): String {
        val result = StringBuilder()
        Vog.d(this, "execRootCmd ---> $cmd")
        try {
            val p = Runtime.getRuntime().exec(cmd)
            DataOutputStream(p.outputStream).use { dos ->
                DataInputStream(p.inputStream).use { dis ->
                    //                    dos.writeBytes(cmd + "\n")
                    dos.flush()
                    dos.writeBytes("exit\n")
                    dos.flush()

                    var line: String? = null
                    while ((dis.readLine().also { line = it }) != null) {
                        result.append(line).append("\n")
                    }
                    p.waitFor()
                }
            }
        } catch (e: Exception) {
            GlobalLog.err(e)
        }
        return result.toString()
    }

    /**
     * 执行命令并且输出结果
     */
    fun execWithSu(cmd: String): String {
        val result = StringBuilder()
        Vog.d(this, "execWithSu ---> $cmd")
        try {
            val p = Runtime.getRuntime().exec("su")// 经过Root处理的android系统即有su命令
            DataOutputStream(p.outputStream).use { dos ->
                DataInputStream(p.inputStream).use { dis ->
                    dos.writeBytes(cmd + "\n")
                    dos.flush()
                    dos.writeBytes("exit\n")
                    dos.flush()

                    var line: String? = null
                    while ((dis.readLine().also { line = it }) != null) {
                        result.append(line).append("\n")
                    }
                    p.waitFor()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result.toString()
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
            e.printStackTrace()
        }
        return result
    }

    fun openAppAccessService(pkg: String, serviceName: String) {
        Vog.d(this, "openAppAccessService ---> $serviceName")
        //同时不关闭其他

        execWithSu(
                "settings put secure enabled_accessibility_services $pkg/$serviceName ${buildList()}\n" +
                        "settings put secure accessibility_enabled 1\n"
        ).also {
            Vog.d(this, "openAppAccessService ---> $it")
        }
    }

    private fun buildList(): String {
        return buildString {
            val am = GlobalApp.APP.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
                    ?.forEach {
                        append(it.id).append(' ')
                    }
        }.also {
            Vog.d(this,"buildList ---> $it")
        }
    }

}
