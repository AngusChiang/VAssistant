package cn.vove7.executorengine.bridges

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.view.KeyEvent
import cn.vove7.common.SystemOperation
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.model.ExResult
import cn.vove7.executorengine.helper.ContactHelper
import cn.vove7.vtp.app.AppHelper
import cn.vove7.vtp.app.AppInfo
import cn.vove7.vtp.hardware.HardwareHelper
import cn.vove7.vtp.log.Vog
import cn.vove7.vtp.system.DeviceInfo
import cn.vove7.vtp.system.SystemHelper


class SystemBridge : SystemOperation {
    private val context: Context = GlobalApp.APP
    private val contactHelper = ContactHelper(context)

    override fun openAppByPkg(pkg: String): ExResult<String> {
        return try {
            val launchIntent = context.packageManager
                    .getLaunchIntentForPackage(pkg)
            if (launchIntent == null) {
                GlobalLog.err("openAppByPkg 启动失败(未找到此App: $pkg")
                Vog.e(this, "openAppByPkg 启动失败(未找到此App: $pkg")
//                Bus.postInfo(MessageEvent("启动失败(未找到此App[pkg:]):$pkg ", WHAT_ERR))
                ExResult("未找到此App: $pkg")
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
            ExResult(e.message)
        }
    }

    /**
     * openAppByWord
     * @return packageName if success
     */
    override fun openAppByWord(appWord: String): ExResult<String> {
        val list = cn.vove7.executorengine.helper.AppHelper(context).matchAppName(appWord)
        return if (list.isNotEmpty()) {
            val info = list[0].data
            Vog.i(this, "打开应用：$appWord -> ${info.name}")

            val o = openAppByPkg(info.packageName)
            if (o.ok)
                ExResult<String>().with(info.packageName)
            else o
        }
//        Bus.postInfo(MessageEvent("未找到应用:$appWord", WHAT_ERR))
        else ExResult("未找到应用:$appWord")
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
            ?: return ExResult("未找到该联系人$s")// "未找到该联系人$s"
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$ph"))
        return try {
            context.startActivity(callIntent)
            ExResult()
        } catch (e: SecurityException) {

            ExResult("无电话权限")
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

    /**
     * 获取App信息
     * @param s 包名 或 App 名
     */
    override fun getAppInfo(s: String): AppInfo? {
        return AppHelper.getAppInfo(context, s, s)
    }

    override fun sendKey(keyCode: Int) {
        sendKeyEvent(keyCode)
    }

    override fun mediaPause() {
        sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE)
    }

    override fun mediaStart() {
        mediaResume()
    }


    private fun sendKeyEvent(keyCode: Int) {
        var ke = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

        intent.putExtra(Intent.EXTRA_KEY_EVENT, ke)
        GlobalApp.APP.sendBroadcast(intent)

        ke = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        intent.putExtra(Intent.EXTRA_KEY_EVENT, ke)
        GlobalApp.APP.sendBroadcast(intent)
    }

    override fun mediaResume() {
        sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY)
    }

    override fun mediaStop() {
        sendKeyEvent(KeyEvent.KEYCODE_MEDIA_STOP)
    }

    override fun isMediaPlaying(): Boolean {
        val am = GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        if (am == null) {
            Vog.e(this, "isMusicActive：无法获取AudioManager引用")
            return false
        }
        return am.isMusicActive
    }
}
