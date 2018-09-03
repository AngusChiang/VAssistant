package cn.vove7.executorengine.bridges

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import cn.vove7.common.SystemOperation
import cn.vove7.common.app.GlobalApp
import cn.vove7.common.app.GlobalLog
import cn.vove7.common.model.ExResult
import cn.vove7.executorengine.helper.AdvanAppHelper
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
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) //清空activity栈
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

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
        val list = AdvanAppHelper.matchAppName(appWord)
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
        sendMediaKey(keyCode)
    }

    override fun mediaPause() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
    }

    override fun mediaStart() {
        mediaResume()
    }


    private fun sendMediaKey(keyCode: Int) {
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
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
    }

    override fun mediaStop() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_STOP)
    }

    override fun mediaNext() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    override fun mediaPre() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    /**
     * 当前音量静音
     */
    override fun volumeMute() {
        val mAudioManager = GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.setStreamMute(AudioManager.USE_DEFAULT_STREAM_TYPE, true)
    }

    override fun volumeUnmute() {
        val mAudioManager = GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.setStreamMute(AudioManager.USE_DEFAULT_STREAM_TYPE, false)
    }

    override fun volumeUp() {
        switchVolume(true)
    }

    override fun volumeDown() {
        switchVolume(false)
    }

    /**
     * @see []
     * @param up Boolean
     */
    fun switchVolume(up: Boolean) {
        val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (up) {
            mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP)
        } else {
            mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER,
                    AudioManager.FX_FOCUS_NAVIGATION_UP)
        }
        Handler().postDelayed({
            mAudioManager.adjustVolume(AudioManager.ADJUST_SAME, 0)
        }, 1000)
    }

    override var musicMaxVolume: Int = -1
        get() {
            val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        }
    override var musicCurrentVolume: Int = -1
        get() {
            val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        }

    override fun setMusicVolume(index: Int) {
        val mAudioManager = GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0)
    }

    override fun setAlarmVolume(index: Int) {
        val mAudioManager = GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, index, 0)
    }

    override fun setNotificationVolume(index: Int) {
        val mAudioManager = GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, index, 0)
    }

    override fun isMediaPlaying(): Boolean {
        val am = GlobalApp.APP.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        if (am == null) {
            Vog.e(this, "isMusicActive：无法获取AudioManager引用")
            return false
        }
        return am.isMusicActive
    }

    override fun vibrate(millis: Long): Boolean {

        val vibrateMan = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrateMan.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrateMan.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrateMan.vibrate(millis)
            }
        }
        return true
    }

    override fun vibrate(arr: Array<Long>): Boolean {
        val vibrateMan = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrateMan.hasVibrator()) {
            val l = LongArray(arr.size)
            var i = 0
            arr.forEach { l[i++] = it }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrateMan.vibrate(VibrationEffect.createWaveform(l, -1))
            } else {
                vibrateMan.vibrate(l, -1)
            }
        }
        return true
    }
}
