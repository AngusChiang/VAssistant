package cn.vove7.executorengine.bridges

import android.content.Context
import android.content.Intent
import android.net.Uri
import cn.vove7.common.SystemOperation
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.model.ExResult
import cn.vove7.executorengine.helper.AppHelper
import cn.vove7.executorengine.helper.ContactHelper
import cn.vove7.vtp.hardware.HardwareHelper
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.system.DeviceInfo
import cn.vove7.vtp.system.SystemHelper


class SystemBridge(private val context: Context) : SystemOperation {
    private val contactHelper = ContactHelper(context)

    override fun openAppByPkg(pkg: String): ExResult<String> {
        return try {
            val launchIntent = context.packageManager
                    .getLaunchIntentForPackage(pkg)
            if (launchIntent == null) {
                GlobalLog.err("openAppByPkg 启动失败(未找到此App: $pkg")
                Vog.e(this, "openAppByPkg 启动失败(未找到此App: $pkg")
//                Bus.postInfo(MessageEvent("启动失败(未找到此App[pkg:]):$pkg ", WHAT_ERR))
                ExResult(null, "未找到此App: $pkg")
            } else {
                launchIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP //清空activity栈
                context.startActivity(launchIntent)
                ExResult()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Vog.wtf(this, "${e.message}")
            GlobalLog.err(e.message ?: "未知错误")
//            Bus.postInfo(MessageEvent("启动失败:$pkg  errMsg:${e.message}", WHAT_ERR))
            ExResult(e.message, "未知错误")
        }
    }

    /**
     * openAppByWord
     * @return packageName if success
     */
    override fun openAppByWord(appWord: String): ExResult<String> {
        val list = AppHelper(context).matchAppName(appWord)
        return if (list.isNotEmpty()) {
            val info = list[0].data
            Vog.i(this, "打开应用：$appWord -> ${info.name}")

            val o = openAppByPkg(info.packageName)
            if (o.ok)
                ExResult(info.packageName)
            else
                o
        }
//        Bus.postInfo(MessageEvent("未找到应用:$appWord", WHAT_ERR))
        else ExResult(null, "未找到应用:$appWord")
    }


    // Open App 启动对应首页Activity
    fun startActivity(pkg: String, fullActivityName: String): Boolean {
        return try {
            val launchIntent = Intent()
            launchIntent.setClassName(pkg, fullActivityName)
            launchIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP //清空activity栈
            context.startActivity(launchIntent)
            true
        } catch (e: Exception) {
            Vog.wtf(this, "${e.message}")
            false
        }
    }

    /**
     * 打电话
     * 优先级：标记 -> 通讯录 -> 服务提供
     */
    override fun call(s: String): ExResult<String> {
        val ph = contactHelper.matchPhone(context, s)
            ?: return ExResult("未找到该联系人$s", "未找到该联系人$s")// "未找到该联系人$s"
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$ph"))
        return try {
            context.startActivity(callIntent)
            ExResult()
        } catch (e: SecurityException) {

            ExResult("无电话权限", "无电话权限")
        } catch (e: Exception) {
            val m = e.message ?: "ERROR: UNKNOWN"
            ExResult(m)
        }
    }

    /**
     * 手电
     */
    override fun openFlashlight(): ExResult<Any> {
        HardwareHelper.switchFlashlight(context, true)
        return ExResult()
    }

    override fun getDeviceInfo(): DeviceInfo {
        return SystemHelper.getDeviceInfo(context)
    }

    override fun openUrl(url: String) {
        SystemHelper.openLink(context, url)
    }
}
