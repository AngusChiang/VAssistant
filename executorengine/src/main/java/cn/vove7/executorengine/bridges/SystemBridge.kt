package cn.vove7.executorengine.bridges

import android.content.Context
import android.content.Intent
import android.net.Uri
import cn.vove7.executorengine.model.PartialResult
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
                Vog.e(this,"openAppByPkg 启动失败(未找到此App[pkg:]):$pkg")
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
     * @return packageName is success
     */
    override fun openAppByWord(appWord: String): String? {//TODO : appWord或跟随指令
        val list = AppHelper(context).matchAppName(appWord)
        if (list.isNotEmpty()) {
            val info = list[0].data
            Vog.i(this, "打开应用：$appWord -> ${info.name}")

            //TODO  检查是偶开启首页Activity
            if(openAppByPkg(info.packageName)) return info.packageName
            else null
        }
//        Bus.postInfo(MessageEvent("未找到应用:$appWord", WHAT_ERR))
        return null
    }


    //TODO : Open App 启动对应首页Activity
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
     * 优先级：标记 -> 通讯录 -> 官方提供
     */
    override fun call(s: String): PartialResult {
        val ph = contactHelper.matchPhone(context, s)
                ?: return PartialResult(false, true, "未找到该联系人$s")
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel://$ph"))
        return try {
            context.startActivity(callIntent)
            PartialResult(true)
        } catch (e: SecurityException) {
            PartialResult(false, msg = "无电话权限")
        }
    }

    /**
     * 手电
     */
    fun openFlashlight(): PartialResult {
        HardwareHelper.switchFlashlight(context, true)
        return PartialResult(true)
    }

}

internal interface SystemOperation {
    /**
     * 通过包名打开App
     */
    fun openAppByPkg(pkg: String): Boolean

    /**
     * 通过通过关键字匹配
     */
    fun openAppByWord(appWord: String): String?

    /**
     * 拨打
     */
    fun call(s: String): PartialResult
}