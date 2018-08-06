package cn.vove7.executorengine.bridges

import android.content.Context
import android.content.Intent
import android.net.Uri
import cn.vove7.common.SystemOperation
import cn.vove7.executorengine.helper.AppHelper
import cn.vove7.executorengine.helper.ContactHelper
import cn.vove7.vtp.hardware.HardwareHelper
import cn.vove7.vtp.log.Vog


class SystemBridge(private val context: Context) : SystemOperation {
    private val contactHelper = ContactHelper(context)

    override fun openAppByPkg(pkg: String): Boolean {
        return try {
            val launchIntent = context.packageManager
                    .getLaunchIntentForPackage(pkg)
            if (launchIntent == null) {
                Vog.e(this, "openAppByPkg 启动失败(未找到此App: $pkg")
//                Bus.postInfo(MessageEvent("启动失败(未找到此App[pkg:]):$pkg ", WHAT_ERR))
                false
            } else {
                launchIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP //清空activity栈
                context.startActivity(launchIntent)
                true
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Vog.wtf(this, "${e.message}")
//            Bus.postInfo(MessageEvent("启动失败:$pkg  msg:${e.message}", WHAT_ERR))
            false
        }
    }

    /**
     * openAppByWord
     * @return packageName if success
     */
    override fun openAppByWord(appWord: String): String? {
        val list = AppHelper(context).matchAppName(appWord)
        return if (list.isNotEmpty()) {
            val info = list[0].data
            Vog.i(this, "打开应用：$appWord -> ${info.name}")

            if (openAppByPkg(info.packageName))
                info.packageName
            else
                null
        }
//        Bus.postInfo(MessageEvent("未找到应用:$appWord", WHAT_ERR))
        else null
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
    override fun call(s: String): String? {
        val ph = contactHelper.matchPhone(context, s)
            ?: return "未找到该联系人$s"
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$ph"))
        return try {
            context.startActivity(callIntent)
            null
        } catch (e: SecurityException) {
            "无电话权限"
        } catch (e: Exception) {
            e.message ?: "ERROR: when call"
        }
    }

    /**
     * 手电
     */
    override fun openFlashlight(): Boolean {
        HardwareHelper.switchFlashlight(context, true)
        return true
    }

}
